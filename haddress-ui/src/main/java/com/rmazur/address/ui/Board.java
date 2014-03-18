package com.rmazur.address.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A board.
 */
public class Board extends ViewGroup {

  /** Main connection width. */
  private static final int MAIN_CONNECTION_WIDTH = 2;

  /** Gestures detector. */
  private GestureDetector gestureDetector;
  private final GestureDetector.OnGestureListener gestureListener = new BoardGestureListener();

  /** Node radius. */
  private int nodeRadius;

  private Map<UiNode, Point> nodes = new LinkedHashMap<>();
  private Map<UiNode, NodeView> viewsMap = new HashMap<>();

  private BoardDelegate delegate;

  private Paint connectionPaint, mainConnectionPaint;

  @SuppressWarnings("UnusedDeclaration")
  public Board(Context context) {
    super(context);
    init(null);
  }

  @SuppressWarnings("UnusedDeclaration")
  public Board(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  @SuppressWarnings("UnusedDeclaration")
  public Board(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(attrs);
  }

  public void setDelegate(final BoardDelegate delegate) {
    this.delegate = delegate;
  }

  private void init(final AttributeSet attrs) {
    Context context = getContext();
    assert context != null;

    gestureDetector = new GestureDetector(context, gestureListener);

    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Board);
      assert a != null;
      nodeRadius = a.getDimensionPixelSize(R.styleable.Board_android_radius, -1);
      if (nodeRadius == -1) {
        throw new IllegalStateException("Node radius is not defined");
      }
      a.recycle();
    }

    @SuppressWarnings("ConstantConditions")
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    connectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    connectionPaint.setColor(Color.GRAY);
    mainConnectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mainConnectionPaint.setColor(Color.BLACK);
    mainConnectionPaint.setStrokeWidth(MAIN_CONNECTION_WIDTH * displayMetrics.density);

    setWillNotDraw(false);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int childrenCount = getChildCount();
    for (int i = 0; i < childrenCount; i++) {
      int sizeSpec = MeasureSpec.makeMeasureSpec(nodeRadius * 2, MeasureSpec.EXACTLY);
      //noinspection ConstantConditions
      measureChild(getChildAt(i), sizeSpec, sizeSpec);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int childrenCount = getChildCount();
    for (int i = 0; i < childrenCount; i++) {
      NodeView nodeView = (NodeView) getChildAt(i);
      //noinspection ConstantConditions
      Point point = nodes.get(nodeView.getNode());
      int hw = nodeView.getMeasuredWidth() / 2, hh = nodeView.getMeasuredHeight() / 2;
      nodeView.layout(point.x - hw, point.y - hh, point.x + hw, point.y + hh);
    }
  }

  @Override
  public boolean onTouchEvent(@SuppressWarnings("NullableProblems") final MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    return true;
  }

  private void drawConnections(final Canvas canvas, final Point src, final Collection<UiNode> connections, final Paint paint) {
    for (UiNode dstNode : connections) {
      Point dst = nodes.get(dstNode);
      canvas.drawLine(src.x, src.y, dst.x, dst.y, paint);
    }
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    for (Map.Entry<UiNode, Point> entry : nodes.entrySet()) {
      Point src = entry.getValue();
      UiNode srcNode = entry.getKey();
      drawConnections(canvas, src, srcNode.getConnections(), connectionPaint);
      drawConnections(canvas, src, srcNode.getMainConnections(), mainConnectionPaint);
    }
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    SavedState state = new SavedState(super.onSaveInstanceState());
    state.data = this.nodes;
    return state;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    SavedState myState = (SavedState) state;
    super.onRestoreInstanceState(myState.getSuperState());
    addAllNodes(myState.data);
  }

  public void clear() {
    removeAllViews();
    nodes.clear();
  }

  @Override
  public void removeAllViews() {
    super.removeAllViews();
    viewsMap.clear();
  }

  public void setNodeRadius(final int radius) {
    if (radius == this.nodeRadius) {
      return;
    }
    this.nodeRadius = radius;
    removeAllViews();
    addAllNodes(nodes);
  }

  public int getNodeRadius() {
    return nodeRadius;
  }

  void addAllNodes(final Map<UiNode, Point> data) {
    for (Map.Entry<UiNode, Point> entry : data.entrySet()) {
      addNodeView(entry.getKey());
    }
    for (Map.Entry<UiNode, Point> entry : data.entrySet()) {
      afterNodeViewAdded(entry.getKey(), entry.getValue());
    }
  }

  void addNode(final float x, final float y) {
    UiNode node = delegate.createNode();
    addNodeView(node);
    afterNodeViewAdded(node, new Point((int) x, (int) y));
  }

  private void addNodeView(UiNode node) {
    NodeView view = new NodeView(getContext());
    view.setNode(node);
    addView(view);
    viewsMap.put(node, view);
  }

  private void afterNodeViewAdded(final UiNode node, final Point point) {
    Point p = new Point();
    LinkedList<UiNode> intersection = new LinkedList<>();
    for (Map.Entry<UiNode, Point> entry : nodes.entrySet()) {
      p.set(point.x, point.y);
      p.offset(-entry.getValue().x, -entry.getValue().y);
      double distance = Math.hypot(Math.abs(p.x), Math.abs(p.y));
      if (distance < nodeRadius) {
        intersection.add(entry.getKey());
      }
    }
    if (!intersection.isEmpty()) {
      for (UiNode affected : intersection) {
        viewsMap.get(affected).invalidate();
      }

      intersection.addFirst(node);
      delegate.onNewIntersection(intersection);
    }

    nodes.put(node, point);
  }

  public static interface BoardDelegate {
    UiNode createNode();
    void onNewIntersection(List<UiNode> intersectedNodes);
  }


  public static class SavedState extends BaseSavedState {

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(Parcel source) {
        return new SavedState(source);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };

    Map<UiNode, Point> data;

    private SavedState(Parcel source) {
      super(source);
      int size = source.readInt();
      data = new HashMap<>(size);
      ClassLoader cl = SavedState.class.getClassLoader();
      for (int i = 0; i < size; i++) {
        UiNode node = source.readParcelable(cl);
        Point point = source.readParcelable(cl);
        data.put(node, point);
      }
    }

    public SavedState(Parcelable superState) {
      super(superState);
    }

    @Override
    public void writeToParcel(@SuppressWarnings("NullableProblems") Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(data.size());
      for (Map.Entry<UiNode, Point> entry : data.entrySet()) {
        dest.writeParcelable(entry.getKey(), flags);
        dest.writeParcelable(entry.getValue(), flags);
      }
    }

  }


  private class BoardGestureListener implements GestureDetector.OnGestureListener {
    @Override
    public boolean onDown(MotionEvent e) {
      return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      addNode(e.getX(), e.getY());
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }
}
