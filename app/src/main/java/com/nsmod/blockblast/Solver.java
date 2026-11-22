package com.nsmod.blockblast;

import java.util.ArrayList;
import java.util.List;

public final class Solver {
    public static final int N = 10;
    public static final int[][][] SHAPES = {
        {{0,0}},                                      // single
        {{0,0},{1,0}},                                // 2 horizontal
        {{0,0},{0,1}},                                // 2 vertical
        {{0,0},{1,0},{2,0}},                          // 3 horizontal
        {{0,0},{0,1},{0,2}},                          // 3 vertical
        {{0,0},{1,0},{0,1},{1,1}},                    // square
        {{0,0},{1,0},{2,0},{0,1},{1,1},{2,1}},        // 3x2
        {{0,0},{0,1},{1,1}},                          // small L
        {{1,0},{0,1},{1,1}},                          // reverse L
        {{0,0},{1,0},{1,1},{2,1}}                     // zigzag
    };
    public static final String[] NAMES = {
        "Single", "2 Horizontal", "2 Vertical", "3 Horizontal",
        "3 Vertical", "2x2 Square", "3x2 Rectangle", "L Shape",
        "Reverse L", "Zigzag"
    };

    public static final class Move {
        public final int shape, row, col, score;
        public Move(int shape, int row, int col, int score) {
            this.shape=shape; this.row=row; this.col=col; this.score=score;
        }
    }

    public static Move best(boolean[][] board, int shapeIndex) {
        int[][] shape = SHAPES[shapeIndex];
        Move best = null;
        for (int r=0;r<N;r++) for (int c=0;c<N;c++) {
            if (!fits(board, shape, r, c)) continue;
            boolean[][] next = copy(board);
            for (int[] p:shape) next[r+p[1]][c+p[0]]=true;
            int cleared = clearLines(next);
            int occupied = 0;
            for (boolean[] row:next) for (boolean v:row) if(v) occupied++;
            int score = cleared*1000 - occupied;
            if (best==null || score>best.score) best=new Move(shapeIndex,r,c,score);
        }
        return best;
    }

    private static boolean fits(boolean[][] b,int[][] s,int r,int c) {
        for(int[] p:s) {
            int x=c+p[0], y=r+p[1];
            if(x<0||x>=N||y<0||y>=N||b[y][x]) return false;
        }
        return true;
    }
    private static boolean[][] copy(boolean[][] b) {
        boolean[][] n=new boolean[N][N];
        for(int i=0;i<N;i++) System.arraycopy(b[i],0,n[i],0,N);
        return n;
    }
    private static int clearLines(boolean[][] b) {
        boolean[] rows=new boolean[N], cols=new boolean[N];
        for(int r=0;r<N;r++){ rows[r]=true; for(int c=0;c<N;c++) rows[r]&=b[r][c]; }
        for(int c=0;c<N;c++){ cols[c]=true; for(int r=0;r<N;r++) cols[c]&=b[r][c]; }
        int count=0;
        for(int r=0;r<N;r++) if(rows[r]){count++; for(int c=0;c<N;c++) b[r][c]=false;}
        for(int c=0;c<N;c++) if(cols[c]){count++; for(int r=0;r<N;r++) b[r][c]=false;}
        return count;
    }
}
