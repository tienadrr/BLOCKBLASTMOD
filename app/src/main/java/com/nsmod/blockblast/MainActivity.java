package com.nsmod.blockblast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity {
    private final boolean[][] board = new boolean[10][10];
    private final Button[][] cells = new Button[10][10];
    private int shapeIndex=0;
    private Solver.Move lastMove;
    private TextView status;
    private EditText left,top,cellSize,sourceX,sourceY;
    private LinearLayout root;
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(12,18,30));
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(12),dp(12),dp(12)); root.setBackgroundColor(Color.rgb(12,18,30));
        ScrollView scroll=new ScrollView(this); scroll.addView(root); setContentView(scroll);

        TextView title=text("BLOCK BLAST • MOD MENU",22,Color.WHITE);
        root.addView(title);
        TextView subtitle=text("Standalone helper • no root",13,0xff9db4cc); root.addView(subtitle);

        LinearLayout row=new LinearLayout(this); root.addView(row);
        addButton(row,"Floating ON",v->enableOverlay());
        addButton(row,"Accessibility",v->startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS")));

        TextView boardTitle=text("BOARD EDITOR — tap occupied cells",15,Color.WHITE);
        LinearLayout.LayoutParams mt=new LinearLayout.LayoutParams(-1,-2); mt.topMargin=dp(12); root.addView(boardTitle,mt);
        GridLayout grid=new GridLayout(this); grid.setColumnCount(10); grid.setRowCount(10);
        for(int r=0;r<10;r++) for(int c=0;c<10;c++){
            final int rr=r,cc=c; Button cell=new Button(this); cells[r][c]=cell;
            cell.setPadding(0,0,0,0); cell.setMinWidth(0); cell.setMinimumWidth(0);
            cell.setMinHeight(0); cell.setMinimumHeight(0); cell.setText("");
            GridLayout.LayoutParams p=new GridLayout.LayoutParams();
            p.width=dp(29); p.height=dp(29); p.setMargins(dp(1),dp(1),dp(1),dp(1));
            grid.addView(cell,p);
            cell.setOnClickListener(v->{board[rr][cc]=!board[rr][cc];lastMove=null;refreshBoard();});
        }
        root.addView(grid);
        LinearLayout boardActions=new LinearLayout(this); root.addView(boardActions);
        addButton(boardActions,"Clear board",v->{for(int r=0;r<10;r++)for(int c=0;c<10;c++)board[r][c]=false;lastMove=null;refreshBoard();});
        addButton(boardActions,"Invert",v->{for(int r=0;r<10;r++)for(int c=0;c<10;c++)board[r][c]=!board[r][c];lastMove=null;refreshBoard();});

        TextView pieceLabel=text("PIECE TO PLACE",15,Color.WHITE); mt=new LinearLayout.LayoutParams(-1,-2);mt.topMargin=dp(12);root.addView(pieceLabel,mt);
        Spinner pieces=new Spinner(this);
        pieces.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Solver.NAMES));
        pieces.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onItemSelected(android.widget.AdapterView<?> a,View v,int pos,long id){shapeIndex=pos;lastMove=null;refreshBoard();}
            public void onNothingSelected(android.widget.AdapterView<?> a){}
        });
        root.addView(pieces);
        LinearLayout actions=new LinearLayout(this);root.addView(actions);
        addButton(actions,"Find best move",v->solve());
        addButton(actions,"Auto drag",v->autoTap());

        status=text("Edit the board, select a piece, then find a move.",13,0xffc4d5e5);
        mt=new LinearLayout.LayoutParams(-1,-2);mt.topMargin=dp(8);root.addView(status,mt);

        TextView cal=text("AUTOPLAY CALIBRATION (screen pixels)",15,Color.WHITE);
        mt=new LinearLayout.LayoutParams(-1,-2);mt.topMargin=dp(14);root.addView(cal,mt);
        left=edit("Board left X"); top=edit("Board top Y"); cellSize=edit("Cell size");
        left.setText(getPref("left","100"));top.setText(getPref("top","400"));cellSize.setText(getPref("cell","32"));
        root.addView(left);root.addView(top);root.addView(cellSize);
        addButton(root,"Save settings",v->saveSettings());
        addButton(root,"Start floating bubble",v->enableOverlay());
        refreshBoard();
    }

    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(5),0,dp(5));return t;}
    private EditText edit(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(Color.WHITE);e.setHintTextColor(0xff8295a8);e.setHint(hint);return e;}
    private void addButton(android.view.ViewGroup parent,String label,View.OnClickListener listener){
        Button b=new Button(this);b.setText(label);b.setTextSize(11);b.setOnClickListener(listener);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(2),dp(3),dp(2),dp(3));
        if(parent instanceof LinearLayout && ((LinearLayout)parent).getOrientation()==LinearLayout.VERTICAL)p.width=-1;
        parent.addView(b,p);
    }
    private void refreshBoard(){
        for(int r=0;r<10;r++)for(int c=0;c<10;c++){
            boolean marked=lastMove!=null && lastMove.row==r && lastMove.col==c;
            boolean inPiece=false;
            if(marked)for(int[] q:Solver.SHAPES[shapeIndex])if(q[0]==c-lastMove.col&&q[1]==r-lastMove.row)inPiece=true;
            cells[r][c].setBackgroundColor(inPiece?0xffffc247:(board[r][c]?0xff3688d8:0xff263447));
        }
    }
    private void solve(){
        lastMove=Solver.best(board,shapeIndex);
        if(lastMove==null){status.setText("No legal placement for "+Solver.NAMES[shapeIndex]+".");}
        else {status.setText("Best placement: row "+(lastMove.row+1)+", column "+(lastMove.col+1)+". Gold cells show the piece.");}
        refreshBoard();
    }
    private void autoTap(){
        if(lastMove==null){status.setText("Find a move first.");return;}
        TapAccessibilityService svc=TapAccessibilityService.get();
        if(svc==null){status.setText("Enable Block Blast Mod Menu in Android Accessibility settings.");return;}
        try{
            float x=Float.parseFloat(left.getText().toString());
            float y=Float.parseFloat(top.getText().toString());
            float size=Float.parseFloat(cellSize.getText().toString());
            float cx=x+(lastMove.col+0.5f)*size;
            float cy=y+(lastMove.row+0.5f)*size;
            svc.tap(cx,cy);
            status.setText("Tap gesture sent to the selected board cell. Verify calibration before use.");
        }catch(Exception ex){status.setText("Calibration values must be valid numbers.");}
    }
    private void enableOverlay(){
        if(!Settings.canDrawOverlays(this)){
            Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
            startActivity(i); return;
        }
        startService(new Intent(this,FloatingService.class));
        status.setText("Floating bubble enabled.");
    }
    private String getPref(String k,String d){return getPreferences(0).getString(k,d);}
    private void saveSettings(){
        getPreferences(0).edit()
            .putString("left", left.getText().toString())
            .putString("top", top.getText().toString())
            .putString("cell", cellSize.getText().toString())
            .apply();
        status.setText("Calibration saved.");
    }
}
