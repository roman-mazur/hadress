package com.rmazur.address.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rmazur.address.Address;
import com.rmazur.address.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main activity.
 */
public class MainActivity extends Activity {

  private Board board;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    board = (Board) findViewById(R.id.board);
    board.setDelegate(new Board.BoardDelegate() {

      @Override
      public UiNode createNode() {
        return new UiNode(new Node(new Address()));
      }

      @Override
      public void onNewIntersection(final List<UiNode> intersectedNodes) {
        try {
          UiNode newNode = intersectedNodes.remove(0);
          Set<Node> connections = new HashSet<>(intersectedNodes.size());
          for (UiNode ui : intersectedNodes) {
            connections.add(ui.getNode());
          }
          newNode.getNode().updateConnections(connections);

          for (Node node : connections) {
            HashSet<Node> newConnections = new HashSet<>(node.getConnections());
            newConnections.add(newNode.getNode());
            node.updateConnections(newConnections);
          }
        } catch (AssertionError e) {
          Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      }

    });

    final TextView radiusLabel = (TextView) findViewById(R.id.radius_label);
    SeekBar radiusBar = (SeekBar) findViewById(R.id.radius_bar);
    DisplayMetrics dm = getResources().getDisplayMetrics();
    final int range = Math.min(dm.widthPixels, dm.heightPixels) / 2;
    final int min = (int) (70 * dm.density);
    radiusLabel.setText(String.valueOf(board.getNodeRadius()));
    radiusBar.setProgress(board.getNodeRadius() - min);
    radiusBar.setMax(min + range);
    radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          int radius = min + progress;
          board.setNodeRadius(radius);
          radiusLabel.setText(String.valueOf(radius));
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = new MenuInflater(this);
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_clear:
        board.clear();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
