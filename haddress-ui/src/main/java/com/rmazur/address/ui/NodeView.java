package com.rmazur.address.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Display a node.
 */
public class NodeView extends View {

  private static final int LABEL_SIZE = 14;

  private Drawable coreDrawable, radiusDrawable;

  private Paint labelPaint;

  private UiNode node;

  public NodeView(Context context) {
    super(context);
    init();
  }

  @SuppressWarnings("UnusedDeclaration")
  public NodeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  @SuppressWarnings("UnusedDeclaration")
  public NodeView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }


  private void init() {
    Resources resources = getResources();
    assert resources != null;
    coreDrawable = resources.getDrawable(R.drawable.node_core);
    radiusDrawable = resources.getDrawable(R.drawable.radius_stroke);

    labelPaint = new Paint(ANTI_ALIAS_FLAG);
    labelPaint.setColor(resources.getColor(R.color.node_label_color));
    labelPaint.setTextAlign(Paint.Align.CENTER);
    labelPaint.setTextSize(LABEL_SIZE * resources.getDisplayMetrics().density);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    int centerX = w / 2;
    int centerY = h / 2;

    coreDrawable.setBounds(
        centerX - coreDrawable.getIntrinsicWidth() / 2,
        centerY - coreDrawable.getIntrinsicHeight() / 2,
        centerX + coreDrawable.getIntrinsicWidth() / 2,
        centerY + coreDrawable.getIntrinsicHeight() / 2
    );

    radiusDrawable.setBounds(0, 0, w, h);
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    radiusDrawable.draw(canvas);
    coreDrawable.draw(canvas);

    int centerX = getWidth() / 2;
    int centerY = getHeight() / 2;

    canvas.drawText(node.getLabel(), centerX, centerY + labelPaint.getTextSize() / 2, labelPaint);
  }

  public UiNode getNode() {
    return node;
  }

  public void setNode(UiNode node) {
    this.node = node;
  }

}
