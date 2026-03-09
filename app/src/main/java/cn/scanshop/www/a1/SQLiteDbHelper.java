package cn.scanshop.www.a1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;
import com.blankj.utilcode.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLiteDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "data.bin";
    public static final String DB_FILE = "/data/data/cn.scanshop.www.a1/databases/data.bin";

    public static final int DB_VERSION = 4;

    public static final String TN_LIST = "lists";
    public static final String TN_UNPLANLISTS = "unplan_lists";
    public static final String TN_UNPLANAJS = "unplan_ajs";
    public static final String TN_UNPLANWEIGHTS = "unplan_weights";
    public static final String TN_UNPLANSEALS = "unplan_seals";
    public static final String TN_UNPLANPHOTOS = "unplan_photos";
    public static final String TN_UNPLANTIMES = "unplan_times";
    public static final String TN_BARS = "bars";

    public static final String EXPORT_FILE = "/sdcard/a1-export.txt";
    //    public static final String BACKUP_DIR = "/sdcard/Download/%tF-%tH-%tM-%tS"; // %tF-%tH-%tM-%tS 的格式类似：2020-1-9-16-41-22
    public static final String BACKUP_FILE = "a1-backup.bin";
    public static final String PHOTO_DIR = "/sdcard/temp"; //照片的存放文件夹，所有上次清除后的照片都在这里，不再分不同的回数和柜号，传到电脑上再对应文件名找出具体的文件
    public static final String BACKUP_ZIP = "/sdcard/Download/a1-backup.zip";
    public static final String BACK_DIR = "/sdcard/backup_temp"; //生成备份文件时用的临时目录
    public static final String BACKUP_DATA = "/sdcard/Download/a1-backup.bin";
    public static final String RESTORE_DIR = "/sdcard/restore_temp"; //恢复备份文件的临时目录

    public static final String CREATE_SQL = "create.sql"; //创建数据库的sql语句

    public static final String FN_ORD = "ord";
    public static final String FN_BAR = "bar";
    public static final String FN_MODEL = "model";
    public static final String FN_NAME = "name";
    public static final String FN_PLANNING = "planning";
    public static final String FN_ORDERNO = "order_no";
    public static final String FN_A = "a";
    public static final String FN_B = "b";
    public static final String FN_C = "c";
    public static final String FN_D = "d";
    public static final String FN_E = "e";
    public static final String FN_F = "f";
    public static final String FN_G = "g";
    public static final String FN_H = "h";
    public static final String FN_I = "i";
    public static final String FN_J = "j";
    public static final String FN_K = "k";
    public static final String FN_L = "l";
    public static final String FN_M = "m";
    public static final String FN_N = "n";
    public static final String FN_O = "o";
    public static final String FN_P = "p";
    public static final String FN_Q = "q";
    public static final String FN_R = "r";
    public static final String FN_S = "s";
    public static final String FN_T = "t";
    public static final String FN_U = "u";
    public static final String FN_V = "v";
    public static final String FN_W = "w";
    public static final String FN_X = "x";
    public static final String FN_Y = "y";
    public static final String FN_Z = "z";
    public static final String FN_AJCODE = "ajcode";

    public static final String VN_ORDERTYPE = "ordertype";

    public static final String FLD_SEP = ",";
    public static final String LINE_SEP = "\r";
    public static final String AJCODE_PATT = "[A-Z]{2,6}[0-9]{5,10}-[A-Z]";
    public static final String AJCODE_PATT_NOAJABLE = "[A-Z]{2,6}[0-9]{5,10}(-[A-Z])?"; //可以不输入aj部分的ajcode格式

    private Context context;

//    //创建 students 表的 sql 语句
//    private static final String CREATE_SQL = "create table " + TN_LIST + "("
//            + "id integer primary key autoincrement,"
//            + "bar NVARCHAR(50), "
//            + "model NVARHCAR(50), "
//            + "planning int, "
//            + "a int, "
//            + "b int, "
//            + "c int, "
//            + "d int, "
//            + "e int, "
//            + "f int, "
//            + "g int, "
//            + "h int, "
//            + "i int, "
//            + "j int"
//            + ");";

    public SQLiteDbHelper(Context context) {
        // 传递数据库名与版本号给父类
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    //读取指定文件的内容
    private String getFileContent(String file) {
        String content = "";
        try {
            InputStream instream = new FileInputStream(file);
            if (instream != null) {
                InputStreamReader inputreader
                        = new InputStreamReader(instream, "UTF-8");
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "";
                //分行读取
                while ((line = buffreader.readLine()) != null) {
                    content += line + "\n";
                }
                instream.close();//关闭输入流
            }
        } catch (java.io.FileNotFoundException e) {
            Log.d("TestFile", "The File doesn't not exist.");
        } catch (IOException e) {
            Log.d("TestFile", e.getMessage());
        }

        return content;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 在这里通过 db.execSQL 函数执行 SQL 语句创建所需要的表

//        db.execSQL(CREATE_SQL);

        try {
            String sqls[] = context.getString(R.string.sql).split(";"); //建立数据库版本2的结构
            for (String sql : sqls) {
                String trim = sql.trim();
                if (!trim.startsWith("--") && !trim.isEmpty()) {
                    db.execSQL(trim);
                    Log.d("q", trim);
                }
            }

            String[] sqls2 = context.getString(R.string.sql_migration_2_3).split(";"); //onCreate()应该是在没有数据库文件的情况下被调用的，所以应该需要建立 版本3 的结构
            for (String sql : sqls2) {
                String trim = sql.trim();
                if (!trim.startsWith("--") && !trim.isEmpty()) {
                    db.execSQL(trim);
                    Log.d("q", trim);
                }
            }

            String[] sqls3 = context.getString(R.string.sql_migration_3_4).split(";"); //支持给临时回数命名
            for (String sql : sqls3) {
                String trim = sql.trim();
                if (!trim.startsWith("--") && !trim.isEmpty()) {
                    db.execSQL(trim);
                    Log.d("q", trim);
                }
            }
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // 数据库版本号变更会调用 onUpgrade 函数，在这根据版本号进行升级数据库
        if (oldVersion == 2 && newVersion == 3) {
            try {
                String[] sqls = context.getString(R.string.sql_migration_2_3).split(";");
                for (String sql : sqls) {
                    String trim = sql.trim();
                    if (!trim.startsWith("--") && !trim.isEmpty()) {
                        db.execSQL(trim);
                        Log.d("q", trim);
                    }
                }
            } catch (SQLException e) {
                Log.e("q-soft", e.getMessage());
                throw e;
            }
        } else if (oldVersion == 3 && newVersion == 4) {
            try {
                String[] sqls = context.getString(R.string.sql_migration_3_4).split(";");
                for (String sql : sqls) {
                    String trim = sql.trim();
                    if (!trim.startsWith("--") && !trim.isEmpty()) {
                        db.execSQL(trim);
                        Log.d("q", trim);
                    }
                }
            } catch (SQLException e) {
                Log.e("q-soft", e.getMessage());
                throw e;
            }
        }
    }

    public class checkDoneException extends Exception {
        final static String MSG = "出货数量已经足够";

        public checkDoneException() {
            super(MSG);
        }
    }

    public void checkProgress() throws checkDoneException //检查应出货数量与实际出货数量的差异，如差异为0，则返回异常，表示不需要出货了
    {
        final String SQL = "SELECT bar, planning - (a+b+c+d+e+f+g+h+i+j) remaining FROM lists WHERE planning >(a+b+c+d+e+f+g+h+i+j) ";
        final String TABLES = "lists a, orders b";
        final String[] COLS = new String[]{"bar", "planning - (a+b+c+d+e+f+g+h+i+j) remaining"};
        final String SEL = "planning >(a+b+c+d+e+f+g+h+i+j)";
        final String[] SELARGS = null;

        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.query(TABLES, COLS, SEL, SELARGS, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String bar = cursor.getString(0);
                int remaining = cursor.getInt(1);
                Log.d("checkProgress", "bar:" + bar + " , " + "remaining:" + remaining);
            }
        } else {
            throw new checkDoneException();
        }
    }

    public void checkProgress2(String orderno) throws checkDoneException //检查应出货数量与实际出货数量的差异，如差异为0，则返回异常，表示不需要出货了
    {
        final String SQL = "SELECT bar, planning-(a + b + c + d + e + f + g + h + i + j) FROM lists WHERE order_no = '?' and lanning-(a + b + c + d + e + f + g + h + i + j) > 0";

        SQLiteDatabase dbw = getReadableDatabase();
        Cursor cursor = dbw.rawQuery(SQL, new String[]{orderno});

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String bar = cursor.getString(0);
                    int remaining = cursor.getInt(1);
                    Log.d("checkProgress", "bar:" + bar + " , " + "remaining:" + remaining);
                }
            } else {
                throw new checkDoneException();
            }
        } finally {
            cursor.close();
            dbw.close();
        }
    }

    public void exists(String bar, String orderno) throws Exception {
        final String SQL = "SELECT id FROM lists WHERE bar = ? AND order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{bar, orderno});
        if (!cursor.moveToNext()) {
            throw new Exception("没有此产品");
        }
    }

    public void update(String bar, String aj, int qty, String orderno) throws Exception {
        try {
            if (getUnchecked(orderno, bar) >= qty) {

                final String SQL = "UPDATE lists SET " + aj + " = " + aj + " + ? WHERE bar = ? AND order_no = ?";
                SQLiteDatabase dbw = getWritableDatabase();
                dbw.execSQL(SQL, new Object[]{qty, bar, orderno});
                dbw.close();

                retime(bar, aj, orderno); //更新最后一次扫描时间
            } else {
                throw new Exception("数量太大");
            }
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        }
    }

    private void retime(String bar, String aj, String orderno) { //更新 回数+条码+柜号（aj） 的最后一次扫描时间
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            final String SQL = "SELECT id FROM times WHERE order_no = ? AND bar = ?";
            Cursor cursor = dbw.rawQuery(SQL, new String[]{orderno, bar});
            if (cursor.getCount() > 0) { //如果已经有记录，更新
                final String SQL2 = "UPDATE times SET " + aj + " = CURRENT_TIMESTAMP, allaj = CURRENT_TIMESTAMP  WHERE order_no = ? AND bar = ?";
                dbw.execSQL(SQL2, new Object[]{orderno, bar});

            } else { //如果没有记录，插入

                final String SQL3 = "INSERT INTO times (order_no, bar, " + aj + " , allaj, " + aj + "_mark) VALUES (?, ?, CURRENT_TIMESTAMP,CURRENT_TIMESTAMP, 0)";
                dbw.execSQL(SQL3, new Object[]{orderno, bar});
            }
            cursor.close();
        } finally {
            dbw.close();
        }
    }

    /**
     * 获得指定型号的条码
     *
     * @param model
     * @return
     */
    public String getBarOfModel(String model) {
        String result = "";
        SQLiteDatabase dbr = getReadableDatabase();
        final String SQL = "SELECT bar FROM lists WHERE model = ?";
        Cursor cursor = dbr.rawQuery(SQL, new String[]{model});
        if (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        return result;
    }

    public void mark(String model, String aj, String orderno, Integer mark) { //标记或取消标记 型号+回数+柜号（aj），本来想用 条码+回数+柜号（aj），但UI中没有显示条码，所以用型号代替
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            final String bar = getBarOfModel(model);
            final String pureaj = getAj(aj);
            final String SQL = "UPDATE times SET " + pureaj + "_mark = ? WHERE order_no = ? AND bar = ?";
            dbw.execSQL(SQL, new Object[]{mark, orderno, bar});
        } finally {
            dbw.close();
        }
    }

    public void alterQty(String bar, String aj, int qty, String orderno) throws Exception {
//        int planning = getLimitOfAj(orderno, bar, aj);
        Pair<Integer, Integer> hint = getAlterHint(orderno, bar, aj);
        int limit = hint.first;
        int ajqty = hint.second;

        if (qty <= limit && qty >= 0) {
            final String pureaj = getAj(aj); //UPDATE 时需要用纯aj，而不能是code-aj
            final String SQL = "UPDATE lists SET " + pureaj + " = ? WHERE bar = ? AND order_no = ?";
            SQLiteDatabase dbw = getWritableDatabase();
            try {
                dbw.execSQL(SQL, new Object[]{qty, bar, orderno});
            } catch (Exception e) {
                throw e;
            } finally {
                dbw.close();
            }
        } else {
            throw new Exception("超出订单数量，或为负数");
        }
    }

    public BarItem get(String bar, String orderno) {
        BarItem result = new BarItem();
        result.model = "(没有此产品)";

        final String SQL = "SELECT ord, model, planning, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z FROM lists WHERE bar = ? AND order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{bar, orderno});
        try {
            if (cursor.moveToNext()) {
                result.ord = cursor.getString(0);
                result.model = cursor.getString(1);
                result.planning = cursor.getInt(2);
                result.a = cursor.getInt(3);
                result.b = cursor.getInt(4);
                result.c = cursor.getInt(5);
                result.d = cursor.getInt(6);
                result.e = cursor.getInt(7);
                result.f = cursor.getInt(8);
                result.g = cursor.getInt(9);
                result.h = cursor.getInt(10);
                result.i = cursor.getInt(11);
                result.j = cursor.getInt(12);
                result.k = cursor.getInt(13);
                result.l = cursor.getInt(14);
                result.m = cursor.getInt(15);
                result.n = cursor.getInt(16);
                result.o = cursor.getInt(17);
                result.p = cursor.getInt(18);
                result.q = cursor.getInt(19);
                result.r = cursor.getInt(20);
                result.s = cursor.getInt(21);
                result.t = cursor.getInt(22);
                result.u = cursor.getInt(23);
                result.v = cursor.getInt(24);
                result.w = cursor.getInt(25);
                result.x = cursor.getInt(26);
                result.y = cursor.getInt(27);
                result.z = cursor.getInt(28);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    public int getUnchecked(String orderno, String bar) {//当前订单（orderno）的指定产品（bar）的未出货数量
        int result = 0;

        final String SQL = "SELECT planning-a-b-c-d-e-f-g-h-i-j-k-l-m-n-o-p-q-r-s-t-u-v-w-x-y-z FROM lists WHERE order_no = ? AND bar = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno, bar});

        try {
            if (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }
        } finally {
//            cursor.close();
//            dbr.close();
        }
        return result;
    }

    public int getUnchecked(String orderno) { //当前订单（orderno）的未出货数量
        int result = 0;

        final String SQL = "SELECT SUM(planning-(a+b+c+d+e+f+g+h+i+j+k+l+m+n+o+p+q+r+s+t+u+v+w+x+y+z)) FROM lists WHERE order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});

        try {
            while (cursor.moveToNext()) {
                result += cursor.getInt(0);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    public int getLimitOfAj(String orderno, String bar, String aj) {//当前订单（orderno）的指定产品（bar）在指定货柜（aj）的可装上限
        int result = 0;

        final String SQL = "SELECT planning-a-b-c-d-e-f-g-h-i-j-k-l-m-n-o-p-q-r-s-t-u-v-w-x-y-z+ " + aj + " FROM lists WHERE order_no = ? AND bar = ?"; //+aj：为了在上限里扣除aj柜的影响，最终的上限是减去除aj柜以外的剩余数量
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno, bar});

        try {
            if (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }
        } finally {
            cursor.close();
            dbr.close();
        }
        return result;
    }

    public Pair<Integer, Integer> getAlterHint(String orderno, String bar, String aj) { //当前订单（orderno）的指定产品（bar）在指定货柜（aj）的当前数量和可装上限，在修改数量的窗口中需要显示出来
        Pair<Integer, Integer> result = new Pair<>(0, 0);

        final String pureaj = getAj(aj); //用getAj()取得纯aj，因查询时需要用纯aj，而不能是code-aj
        final String SQL = "SELECT planning-a-b-c-d-e-f-g-h-i-j-k-l-m-n-o-p-q-r-s-t-u-v-w-x-y-z, " + pureaj + " FROM lists WHERE order_no = ? AND bar = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno, bar});

        try {
            if (cursor.moveToNext()) {
                int unchecked = cursor.getInt(0); //这是未装的数量
                int ajqty = cursor.getInt(1); //这是aj柜的当前数量
                int limit = unchecked + ajqty; //这是加上aj柜的数量后的未装数量，因为修改数量时，是指aj柜的可装上限，所以需要把已装的数量忽略掉
                result = new Pair<>(limit, ajqty);
            }
        } finally {
            cursor.close();
            dbr.close();
        }
        return result;
    }


    public List<BarItem> getStats(String orderno, String orderby) { //查询出指定订单号（orderno）的所有产品的出货情况
        List<BarItem> result = new ArrayList<BarItem>();

        final String SQL = "SELECT t1.ord, t1.model, t1.planning, t1.a,t1.b,t1.c,t1.d,t1.e,t1.f,t1.g,t1.h,t1.i,t1.j,t1.k,t1.l,t1.m,t1.n,t1.o,t1.p,t1.q,t1.r,t1.s,t1.t,t1.u,t1.v,t1.w,t1.x,t1.y,t1.z  FROM lists t1 LEFT JOIN times t2 ON t1.order_no = t2.order_no AND t1.bar = t2.bar WHERE t1.order_no = ? AND t1.planning-t1.a-t1.b-t1.c-t1.d-t1.e-t1.f-t1.g-t1.h-t1.i-t1.j-t1.k-t1.l-t1.m-t1.n-t1.o-t1.p-t1.q-t1.r-t1.s-t1.t-t1.u-t1.v-t1.w-t1.x-t1.y-t1.z <> 0 "; //不用查询 total 和 unchecked，在 BarItem() 里会算出来；只显示装数与计划有差异的行

        final String ORD_ORDER = " ORDER BY t1.ord "; //按 序号 排序
        final String SCAN_ORDER = " ORDER BY IFNULL(t2.allaj, '9999-12-31') "; //t2.allaj 是整个订单的的每个柜的一个产品的最后扫描时间

        String sql = SQL;
        if (orderby == "scan") {
            sql = sql + SCAN_ORDER;
        } else if (orderby == "ord") {
            sql = sql + ORD_ORDER;
        }

        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(sql, new String[]{orderno});

        try {
            while (cursor.moveToNext()) {
                BarItem bi = new BarItem();
                bi.ord = cursor.getString(0);
                bi.model = cursor.getString(1);
                bi.planning = cursor.getInt(2);
                bi.a = cursor.getInt(3);
                bi.b = cursor.getInt(4);
                bi.c = cursor.getInt(5);
                bi.d = cursor.getInt(6);
                bi.e = cursor.getInt(7);
                bi.f = cursor.getInt(8);
                bi.g = cursor.getInt(9);
                bi.h = cursor.getInt(10);
                bi.i = cursor.getInt(11);
                bi.j = cursor.getInt(12);
                bi.k = cursor.getInt(13);
                bi.l = cursor.getInt(14);
                bi.m = cursor.getInt(15);
                bi.n = cursor.getInt(16);
                bi.o = cursor.getInt(17);
                bi.p = cursor.getInt(18);
                bi.q = cursor.getInt(19);
                bi.r = cursor.getInt(20);
                bi.s = cursor.getInt(21);
                bi.t = cursor.getInt(22);
                bi.u = cursor.getInt(23);
                bi.v = cursor.getInt(24);
                bi.w = cursor.getInt(25);
                bi.x = cursor.getInt(26);
                bi.y = cursor.getInt(27);
                bi.z = cursor.getInt(28);
                result.add(bi);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

//    public int getPlanning(String bar, String orderno) {
//        int result = 0;
//
//        final String SQL = "SELECT planning FROM lists WHERE bar = ? AND order_no = ?";
//        SQLiteDatabase dbr = getReadableDatabase();
//        try {
//            Cursor cursor = dbr.rawQuery(SQL, new String[]{bar, orderno});
//            if (cursor.moveToNext()) {
//                result = cursor.getInt(0);
//            }
//        } catch (Exception e) {
//            Log.e("q", e.getMessage());
//            throw e;
//        } finally {
//            dbr.close();
//        }
//
//        return result;
//    }

//    public int unchecked(String bar, String orderno) { //检查指定订单（orderno）的指定条码（bar）未出货的数量
//
//        int unchecked = 0;
//
//        final String SQL = "SELECT planning - (a+b+c+d+e+f+g+h+i+j) FROM lists WHERE bar = '?' AND order_no = '?'";
//        SQLiteDatabase dbr = getReadableDatabase();
//        Cursor cursor = dbr.rawQuery(SQL, new String[]{bar, orderno});
//
//        try {
//            if (cursor.getCount() > 0) {
//                unchecked = cursor.getInt(0);
//            }
//        } finally {
//            cursor.close();
//            dbr.close();
//        }
//
//        return unchecked;
//    }

    public void mockInitList() {
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            final String CLEAR = "delete from lists";
            dbw.execSQL(CLEAR);

            final String CLEAR2 = "delete from ajs";
            dbw.execSQL(CLEAR2);

            final String CLEAR3 = "delete from weights";
            dbw.execSQL(CLEAR3);

            final String CLEAR4 = "delete from seals";
            dbw.execSQL(CLEAR4);

            final String CLEAR5 = "delete from photos";
            dbw.execSQL(CLEAR5);

            final String CLEAR6 = "delete from unplan_lists";
            dbw.execSQL(CLEAR6);

            final String CLEAR7 = "delete from unplan_ajs";
            dbw.execSQL(CLEAR7);

            final String CLEAR8 = "delete from unplan_weights";
            dbw.execSQL(CLEAR8);

            final String CLEAR9 = "delete from unplan_seals";
            dbw.execSQL(CLEAR9);

            final String CLEAR10 = "delete from unplan_photos";
            dbw.execSQL(CLEAR10);

            final String CLEAR11 = "delete from bars";
            dbw.execSQL(CLEAR11);

            final String SQL = "insert into lists (bar, model, name, planning, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z, ord, order_no) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            dbw.execSQL(SQL, new Object[]{"4550283168943", "m-1", "n-1", 100, 10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, "order-1"});
            dbw.execSQL(SQL, new Object[]{"518817167238", "m-2", "n-2", 100, 10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, "order-1"});
            dbw.execSQL(SQL, new Object[]{"518817167239", "m-3", "n-3", 100, 10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, "order-1"});

            final String SQL2 = "insert into ajs (order_no, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            dbw.execSQL(SQL2, new Object[]{"order-1", "ABCD1234567", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});

            final String SQL3 = "insert into weights (order_no, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            dbw.execSQL(SQL3, new Object[]{"order-1", "999", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});

            final String SQL4 = "insert into seals (order_no, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            dbw.execSQL(SQL4, new Object[]{"order-1", "SEAL-1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});

            final String SQL5 = "insert into photos (order_no, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            dbw.execSQL(SQL5, new Object[]{"order-1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});

            final String SQL6 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-1','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL6);

            final String SQL7 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-2','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL7);

            final String SQL8 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-3','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL8);

            final String SQL9 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-4','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL9);

            final String SQL10 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-5','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL10);

            final String SQL11 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-6','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL11);

            final String SQL12 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-7','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL12);

            final String SQL13 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-8','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL13);

            final String SQL14 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-9','','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL14);

            final String SQL15 = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES('临时调货-10','TEMP123456','','','','','','','','','','','','','','','','','','','','','','','','','')";
            dbw.execSQL(SQL15);

            final String SQL16 = "insert into unplan_lists (bar, model, name, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,order_no) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            dbw.execSQL(SQL16, new Object[]{"81039142205940242366", "m-1", "n-1", 10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "临时调货-10"});
            dbw.execSQL(SQL16, new Object[]{"518817167238", "m-2", "n-2", 10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "临时调货-10"});
            dbw.execSQL(SQL16, new Object[]{"518817167239", "m-3", "n-3", 10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "临时调货-10"});

            dbw.close();
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        } finally {
            dbw.close();
        }
    }

    public List<String> getOrders() {
        List<String> result = new ArrayList();

        final String SQL = "select distinct `order_no` from lists";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{});

        try {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    public List<String> getAjs(String orderno) { //获得指定回数（订单）的货柜号清单
        List<String> result = new ArrayList<>();

        final String SQL = "SELECT a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z FROM ajs WHERE order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});

        try {

            if (cursor.moveToNext()) {
                String a = cursor.getString(0);
                String b = cursor.getString(1);
                String c = cursor.getString(2);
                String d = cursor.getString(3);
                String e = cursor.getString(4);
                String f = cursor.getString(5);
                String g = cursor.getString(6);
                String h = cursor.getString(7);
                String i = cursor.getString(8);
                String j = cursor.getString(9);
                String k = cursor.getString(10) != null ? cursor.getString(10) : "";
                String l = cursor.getString(11) != null ? cursor.getString(11) : "";
                String m = cursor.getString(12) != null ? cursor.getString(12) : "";
                String n = cursor.getString(13) != null ? cursor.getString(13) : "";
                String o = cursor.getString(14) != null ? cursor.getString(14) : "";
                String p = cursor.getString(15) != null ? cursor.getString(15) : "";
                String q = cursor.getString(16) != null ? cursor.getString(16) : "";
                String r = cursor.getString(17) != null ? cursor.getString(17) : "";
                String s = cursor.getString(18) != null ? cursor.getString(18) : "";
                String t = cursor.getString(19) != null ? cursor.getString(19) : "";
                String u = cursor.getString(20) != null ? cursor.getString(20) : "";
                String v = cursor.getString(21) != null ? cursor.getString(21) : "";
                String w = cursor.getString(22) != null ? cursor.getString(22) : "";
                String x = cursor.getString(23) != null ? cursor.getString(23) : "";
                String y = cursor.getString(24) != null ? cursor.getString(24) : "";
                String z = cursor.getString(25) != null ? cursor.getString(25) : "";

                a = String.format("%s-%s", a, "A");
                b = String.format("%s-%s", b, "B");
                c = String.format("%s-%s", c, "C");
                d = String.format("%s-%s", d, "D");
                e = String.format("%s-%s", e, "E");
                f = String.format("%s-%s", f, "F");
                g = String.format("%s-%s", g, "G");
                h = String.format("%s-%s", h, "H");
                i = String.format("%s-%s", i, "I");
                j = String.format("%s-%s", j, "J");
                k = String.format("%s-%s", k, "K");
                l = String.format("%s-%s", l, "L");
                m = String.format("%s-%s", m, "M");
                n = String.format("%s-%s", n, "N");
                o = String.format("%s-%s", o, "O");
                p = String.format("%s-%s", p, "P");
                q = String.format("%s-%s", q, "Q");
                r = String.format("%s-%s", r, "R");
                s = String.format("%s-%s", s, "S");
                t = String.format("%s-%s", t, "T");
                u = String.format("%s-%s", u, "U");
                v = String.format("%s-%s", v, "V");
                w = String.format("%s-%s", w, "W");
                x = String.format("%s-%s", x, "X");
                y = String.format("%s-%s", y, "Y");
                z = String.format("%s-%s", z, "Z");

                result.addAll(Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z));
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    public String getAj(String aj) { //取得aj的纯aj部分
        String result = "";

        Pattern p = Pattern.compile("\\w*-([A-Z])");
        Matcher m = p.matcher(aj);

        Log.d("q", "m.matches():" + m.matches());

        result = m.group(1);

        Log.d("q", "getAj() result:" + result);

        return result;
    }

    public String getCode(String ajcode) { //取得ajcode的code部分
        String result = "";

        Pattern p = Pattern.compile("([A-Z]{2,6}[0-9]{5,10})(-[A-Z])?");
        Matcher m = p.matcher(ajcode);

        Log.d("q", "m.matches():" + m.matches());

        result = m.group(1);

        Log.d("q", "getAj() result:" + result);

        return result;
    }

    public void setAj(String aj, String ajcode, String orderno) {
        final String SQL = "UPDATE ajs SET " + aj + " = ? WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();

        try {
            dbw.execSQL(SQL, new Object[]{ajcode, orderno});
        } catch (SQLException e) {
            Log.e("q", e.getMessage());
        }

    }

    public List<String> getAjcodes() { //取得完整的aj柜号+aj柜名的清单
        List<String> result = new ArrayList<>();

        final String SQL = "SELECT order_no, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z FROM ajs";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            final String orderno = cursor.getString(0);
            String a = cursor.getString(1) + "-A";
            String b = cursor.getString(2) + "-B";
            String c = cursor.getString(3) + "-C";
            String d = cursor.getString(4) + "-D";
            String e = cursor.getString(5) + "-E";
            String f = cursor.getString(6) + "-F";
            String g = cursor.getString(7) + "-G";
            String h = cursor.getString(8) + "-H";
            String i = cursor.getString(9) + "-I";
            String j = cursor.getString(10) + "-J";
            String k = cursor.getString(11) + "-K";
            String l = cursor.getString(12) + "-L";
            String m = cursor.getString(13) + "-M";
            String n = cursor.getString(14) + "-N";
            String o = cursor.getString(15) + "-O";
            String p = cursor.getString(16) + "-P";
            String q = cursor.getString(17) + "-Q";
            String r = cursor.getString(18) + "-R";
            String s = cursor.getString(19) + "-S";
            String t = cursor.getString(20) + "-T";
            String u = cursor.getString(21) + "-U";
            String v = cursor.getString(22) + "-V";
            String w = cursor.getString(23) + "-W";
            String x = cursor.getString(24) + "-X";
            String y = cursor.getString(25) + "-Y";
            String z = cursor.getString(26) + "-Z";

            final List<String> list = Arrays.asList(orderno, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z);
            final String line = Util.Companion.joinString(list, SQLiteDbHelper.FLD_SEP);
            result.add(line);
        }

        return result;
    }

    public List<String> exportList() {
        List<String> result = new ArrayList<String>();

//        final String SQL = "SELECT bar, model, name, planning, a+b+c+d+e+f+g+h+i+j total FROM lists";
        final String SQL = "SELECT bar, model, name, a+b+c+d+e+f+g+h+i+j+k+l+m+n+o+p+q+r+s+t+u+v+w+x+y+z total FROM lists";

        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{});
        while (cursor.moveToNext()) {
            final String bar = cursor.getString(0);
            final String model = cursor.getString(1);
            final String name = cursor.getString(2);
//            final String planning = cursor.getString(3);
            final String total = cursor.getString(3);

//            final String line = String.format("%s,%s,%s,%s,%s", bar, model, name, planning, total);
            final String line = String.format("%s,%s,%s,%s", bar, model, name, total);

            result.add(line);
        }

        return result;
    }

    public List<String> exportList2() {
        List<String> result = new ArrayList<String>();

        //每行格式：bar,model,name,a,b,c,d,e,f,g,h,i,j,order_no
        final String SQL = "SELECT bar, model, name, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z order_no FROM lists";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, null);
        int ii = 0;
        while (cursor.moveToNext()) {
            String bar = cursor.getString(0);
            String model = cursor.getString(1);
            String name = cursor.getString(2);
            String a = cursor.getString(3);
            String b = cursor.getString(4);
            String c = cursor.getString(5);
            String d = cursor.getString(6);
            String e = cursor.getString(7);
            String f = cursor.getString(8);
            String g = cursor.getString(9);
            String h = cursor.getString(10);
            String i = cursor.getString(11);
            String j = cursor.getString(12);
            String k = cursor.getString(13);
            String l = cursor.getString(14);
            String m = cursor.getString(15);
            String n = cursor.getString(16);
            String o = cursor.getString(17);
            String p = cursor.getString(18);
            String q = cursor.getString(19);
            String r = cursor.getString(20);
            String s = cursor.getString(21);
            String t = cursor.getString(22);
            String u = cursor.getString(23);
            String v = cursor.getString(24);
            String w = cursor.getString(25);
            String x = cursor.getString(26);
            String y = cursor.getString(27);
            String z = cursor.getString(28);
            String orderno = cursor.getString(13);

            List<String> list = Arrays.asList(bar, model, name, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, orderno);
            result.add(Util.Companion.joinString(list, SQLiteDbHelper.FLD_SEP)); //java太难用了，连个join()都没有，只能用kotlin封闭一个来用

            Log.d("q", ii + "," + Util.Companion.joinString(list, SQLiteDbHelper.LINE_SEP));
        }

        return result;
    }

    public List<String> exportList3(String orderno, String ajcode, String weight, String seal) { //只传送指定回数（orderno）的指定柜（aj）
        List<String> result = new ArrayList<>();

        final String pureaj = getAj(ajcode);
        //每行格式：bar,model,name,aj柜的装数
        final String SQL = "SELECT ord, bar, model, name, " + pureaj + " FROM lists WHERE order_no = ? AND " + pureaj + " <> 0";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
        while (cursor.moveToNext()) {
            final String ord = cursor.getString(0);
            final String bar = cursor.getString(1);
            final String model = cursor.getString(2);
            final String name = cursor.getString(3);
            final String ajj = cursor.getString(4);

            List<String> list = Arrays.asList(ord, bar, model, name, ajj);
            result.add(Util.Companion.joinString(list, SQLiteDbHelper.FLD_SEP));
        }

        List<String> exportlist = new ArrayList<>(result);
        exportlist.add(0, orderno + "---" + ajcode); //插入第1行为本次传送的 回数---ajcode
        exportlist.add(1, weight + "---" + seal); //插入第2行为 柜重---封条
        Util.Companion.overwriteToFile(exportlist, EXPORT_FILE);

        return result;
    }

    public List<String> exportList4(String orderno, String ajcode, String weight, String seal) { //只传送指定回数（orderno）的指定柜（aj）；并且上传扫描时间
        if (orderno.matches("^临时调货-\\d+$")) { //临时调货扫描
            final String alias = unplanGetOrderAlias(orderno);
            return unplanExportList5(orderno, ajcode, weight, seal, alias);
        } else { //有计划的回数
            return exportList5(orderno, ajcode, weight, seal);
        }
    }

    /**
     * 导出有计划的回数数据
     *
     * @param orderno
     * @param ajcode
     * @param weight
     * @param seal
     * @return
     */
    public List<String> exportList5(String orderno, String ajcode, String weight, String seal) {
        List<String> result = new ArrayList<>();

        final String pureaj = getAj(ajcode);
        //每行格式：bar,model,name,aj柜的装数
        final String SQL = "SELECT t1.ord, t1.bar, t1.model, t1.name, t1." + pureaj + " , t2." + pureaj + " FROM lists t1 INNER JOIN times t2 ON t1.order_no = t2.order_no AND t1.bar = t2.bar WHERE t1.order_no = ? AND t1." + pureaj + " <> 0";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
        while (cursor.moveToNext()) {
            final String ord = cursor.getString(0);
            final String bar = cursor.getString(1);
            final String model = cursor.getString(2);
            final String name = cursor.getString(3);
            final String ajj = cursor.getString(4);
            final String stime = cursor.getString(5); //扫描时间

            List<String> list = Arrays.asList(ord, bar, model, name, ajj, stime);
            result.add(Util.Companion.joinString(list, SQLiteDbHelper.FLD_SEP));
        }

        List<String> exportlist = new ArrayList<>(result);
        exportlist.add(0, orderno + "---" + ajcode); //插入第1行为本次传送的 回数---ajcode
        exportlist.add(1, weight + "---" + seal); //插入第2行为 柜重---封条
//        Util.Companion.overwriteToFile(exportlist, EXPORT_FILE);  //出现写文件被拒绝，未知原因，暂时不要

        return result;
    }

    /**
     * 导出临时调货扫描的数据
     *
     * @param orderno
     * @param ajcode
     * @param weight
     * @param seal
     * @return
     */
    public List<String> unplanExportList5(String orderno, String ajcode, String weight, String seal, String alias) {
        List<String> result = new ArrayList<>();

        final String pureaj = getAj(ajcode);
        //每行格式：bar,model,name,aj柜的装数
        final String SQL = "SELECT t1.bar, t1.model, t1.name, t1." + pureaj + " , t2." + pureaj + " FROM unplan_lists t1 INNER JOIN unplan_times t2 ON t1.order_no = t2.order_no AND t1.bar = t2.bar WHERE t1.order_no = ? AND t1." + pureaj + " <> 0";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
        int pos = 1;
        while (cursor.moveToNext()) {
            final String ord = String.valueOf(pos); //临时回数没有位置号，固定设置为0，使得电脑接收时格式与普通回数一致
            final String bar = cursor.getString(0);
            final String model = cursor.getString(1);
            final String name = cursor.getString(2);
            final String ajj = cursor.getString(3);
            final String stime = cursor.getString(4); //扫描时间

            List<String> list = Arrays.asList(ord, bar, model, name, ajj, stime);
            result.add(Util.Companion.joinString(list, SQLiteDbHelper.FLD_SEP));
            pos++;
        }

        List<String> exportlist = new ArrayList<>(result);
        exportlist.add(0, alias + "---" + ajcode); //插入第1行为本次传送的 回数---ajcode
        exportlist.add(1, weight + "---" + seal); //插入第2行为 柜重---封条
//        Util.Companion.overwriteToFile(exportlist, EXPORT_FILE);  //出现写文件被拒绝，未知原因，暂时不要

        return result;
    }

    public void importList(List<String> list) {
        SQLiteDatabase dbw = getWritableDatabase();

        try {
//            final String CLEAR = "DELETE FROM lists"; //不应在导入时清除本机数据，后期会增加选择后删除功能
//            dbw.execSQL(CLEAR);

            List<String> orders = new ArrayList<>(); //这是本次收到的回数

            ContentValues cv = new ContentValues();

            for (String line : list) {
                final String[] flds = line.split(",");
                final String ord = flds[0];
                final String bar = flds[1];
                final String model = flds[2];
                final String name = flds[3];
                final String planning = flds[4];
                final String orderno = flds[5];

                if (orders.indexOf(orderno) == -1) {
                    orders.add(orderno);
                }

                cv.clear();
                cv.put(FN_ORD, ord);
                cv.put(FN_BAR, bar);
                cv.put(FN_MODEL, model);
                cv.put(FN_NAME, name);
                cv.put(FN_PLANNING, planning);
                cv.put(FN_A, 0);
                cv.put(FN_B, 0);
                cv.put(FN_C, 0);
                cv.put(FN_D, 0);
                cv.put(FN_E, 0);
                cv.put(FN_F, 0);
                cv.put(FN_G, 0);
                cv.put(FN_H, 0);
                cv.put(FN_I, 0);
                cv.put(FN_J, 0);
                cv.put(FN_K, 0);
                cv.put(FN_L, 0);
                cv.put(FN_M, 0);
                cv.put(FN_N, 0);
                cv.put(FN_O, 0);
                cv.put(FN_P, 0);
                cv.put(FN_Q, 0);
                cv.put(FN_R, 0);
                cv.put(FN_S, 0);
                cv.put(FN_T, 0);
                cv.put(FN_U, 0);
                cv.put(FN_V, 0);
                cv.put(FN_W, 0);
                cv.put(FN_X, 0);
                cv.put(FN_Y, 0);
                cv.put(FN_Z, 0);

                cv.put(FN_ORDERNO, orderno);

                dbw.insert(TN_LIST, null, cv);

                Log.d("q", "importList():" + line);
            }

            for (String order : orders) {
                final String SQL = "INSERT INTO ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES (?,'','','','','','','','','','','','','','','','','','','','','','','','','','')";
                dbw.execSQL(SQL, new Object[]{order});

                final String SQL2 = "INSERT INTO weights (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES (?,'','','','','','','','','','','','','','','','','','','','','','','','','','')";
                dbw.execSQL(SQL2, new Object[]{order});

                final String SQL3 = "INSERT INTO seals (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES (?,'','','','','','','','','','','','','','','','','','','','','','','','','','')";
                dbw.execSQL(SQL3, new Object[]{order});

                final String SQL4 = "INSERT INTO photos (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES (?,'','','','','','','','','','','','','','','','','','','','','','','','','','')";
                dbw.execSQL(SQL4, new Object[]{order});
            }

            dbw.close();
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        }
    }


    public List<String> getAjOfOrder(String orderno) //取得一个回数的code-aj，对于纯aj不取（纯aj指aj柜号，而不是柜名的项目），因纯aj肯定是没有装过的
    {
        List<String> result = new ArrayList<>();

        final String SQL = "SELECT a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z FROM ajs WHERE order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
        if (cursor.moveToNext()) {
            String a = cursor.getString(0);
            String b = cursor.getString(1);
            String c = cursor.getString(2);
            String d = cursor.getString(3);
            String e = cursor.getString(4);
            String f = cursor.getString(5);
            String g = cursor.getString(6);
            String h = cursor.getString(7);
            String i = cursor.getString(8);
            String j = cursor.getString(9);
            String k = cursor.getString(10) != null ? cursor.getString(10) : "";
            String l = cursor.getString(11) != null ? cursor.getString(11) : "";
            String m = cursor.getString(12) != null ? cursor.getString(12) : "";
            String n = cursor.getString(13) != null ? cursor.getString(13) : "";
            String o = cursor.getString(14) != null ? cursor.getString(14) : "";
            String p = cursor.getString(15) != null ? cursor.getString(15) : "";
            String q = cursor.getString(16) != null ? cursor.getString(16) : "";
            String r = cursor.getString(17) != null ? cursor.getString(17) : "";
            String s = cursor.getString(18) != null ? cursor.getString(18) : "";
            String t = cursor.getString(19) != null ? cursor.getString(19) : "";
            String u = cursor.getString(20) != null ? cursor.getString(20) : "";
            String v = cursor.getString(21) != null ? cursor.getString(21) : "";
            String w = cursor.getString(22) != null ? cursor.getString(22) : "";
            String x = cursor.getString(23) != null ? cursor.getString(23) : "";
            String y = cursor.getString(24) != null ? cursor.getString(24) : "";
            String z = cursor.getString(25) != null ? cursor.getString(25) : "";

            Pattern patt = Pattern.compile("[A-Z]{2,6}[0-9]{5,10}");
            if (patt.matcher(a).matches()) {
                result.add(a + "-A");
            }
            if (patt.matcher(b).matches()) {
                result.add(b + "-B");
            }
            if (patt.matcher(c).matches()) {
                result.add(c + "-C");
            }
            if (patt.matcher(d).matches()) {
                result.add(d + "-D");
            }
            if (patt.matcher(e).matches()) {
                result.add(e + "-E");
            }
            if (patt.matcher(f).matches()) {
                result.add(f + "-F");
            }
            if (patt.matcher(g).matches()) {
                result.add(g + "-G");
            }
            if (patt.matcher(h).matches()) {
                result.add(h + "-H");
            }
            if (patt.matcher(i).matches()) {
                result.add(i + "-I");
            }
            if (patt.matcher(j).matches()) {
                result.add(j + "-J");
            }
            if (k != null && patt.matcher(k).matches()) {
                result.add(k + "-K");
            }
            if (l != null && patt.matcher(l).matches()) {
                result.add(l + "-L");
            }
            if (m != null && patt.matcher(m).matches()) {
                result.add(m + "-M");
            }
            if (n != null && patt.matcher(n).matches()) {
                result.add(n + "-N");
            }
            if (o != null && patt.matcher(o).matches()) {
                result.add(o + "-O");
            }
            if (p != null && patt.matcher(p).matches()) {
                result.add(p + "-P");
            }
            if (q != null && patt.matcher(q).matches()) {
                result.add(q + "-Q");
            }
            if (r != null && patt.matcher(r).matches()) {
                result.add(r + "-R");
            }
            if (s != null && patt.matcher(s).matches()) {
                result.add(s + "-S");
            }
            if (t != null && patt.matcher(t).matches()) {
                result.add(t + "-T");
            }
            if (u != null && patt.matcher(u).matches()) {
                result.add(u + "-U");
            }
            if (v != null && patt.matcher(v).matches()) {
                result.add(v + "-V");
            }
            if (w != null && patt.matcher(w).matches()) {
                result.add(w + "-W");
            }
            if (x != null && patt.matcher(x).matches()) {
                result.add(x + "-X");
            }
            if (y != null && patt.matcher(y).matches()) {
                result.add(y + "-Y");
            }
            if (z != null && patt.matcher(z).matches()) {
                result.add(z + "-Z");
            }
        }

        return result;
    }

    public List<BarItemOfAj> getStatsOfAj(String orderno, String aj, String orderby) { //查询出指定订单号（orderno）+柜号（aj）的的装柜情况
        List<BarItemOfAj> result = new ArrayList<BarItemOfAj>();

        final String pureaj = getAj(aj);

        final String SQL = "SELECT t1.ord, t1.model, t1.planning, t1." + pureaj + ", t1.a+t1.b+t1.c+t1.d+t1.e+t1.f+t1.g+t1.h+t1.i+t1.j+t1.k+t1.l+t1.m+t1.n+t1.o+t1.p+t1.q+t1.r+t1.s+t1.t+t1.u+t1.v+t1.w+t1.x+t1.y+t1.z, t2." + pureaj + "_mark FROM lists t1 INNER JOIN times t2 ON t1.order_no = t2.order_no AND t1.bar = t2.bar  WHERE t1.order_no = ? AND t1." + pureaj + " <> 0 "; //不用查询 unchecked，在 BarItemOfAj() 里会算出来

        final String SCAN_ORDER = " ORDER BY t2." + pureaj;
        final String ORD_ORDER = " ORDER BY t1.ord ";
        String sql = SQL;
        if (orderby == "scan") { //按扫描顺序排
            sql = sql + SCAN_ORDER;
        } else {
            sql = sql + ORD_ORDER; //按序号排
        }

        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(sql, new String[]{orderno});

        try {
            while (cursor.moveToNext()) {
                BarItemOfAj bi = new BarItemOfAj();
                bi.ord = cursor.getString(0);
                bi.model = cursor.getString(1);
                bi.planning = cursor.getInt(2);
                bi.done = cursor.getInt(3);
                bi.otheraj = cursor.getInt(4) - bi.done; //其他柜的装数 = 总装数 - 本柜装数
                bi.mark = cursor.getInt(5); //是否标记为黄色，0表示未标记，1表示标记
                result.add(bi);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    public void delAj(String orderno, String ajcode) { //删除指定回数（orderno)的指定柜号（ajcode）。FIXME：柜重 和 封条 好像没有清除
        final String pureaj = getAj(ajcode);
        final String SQL = "UPDATE lists SET " + pureaj + " = 0 WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL(SQL, new Object[]{orderno});

        final String SQL2 = "UPDATE ajs SET " + pureaj + " = '' WHERE order_no = ?";
        dbw.execSQL(SQL2, new Object[]{orderno});

        final String SQL3 = "UPDATE times SET  " + pureaj + " = null, " + pureaj + "_mark = 0 WHERE order_no = ? "; //清除扫描次序与加黄标记
        dbw.execSQL(SQL3, new Object[]{orderno});

        final String SQL4 = "UPDATE photos SET " + pureaj + " = '' WHERE order_no = ?"; //清除照片
        dbw.execSQL(SQL4, new Object[]{orderno});

        dbw.close();
    }

    public void delOrder(String orderno) { //删除指定的回数（orderno）
        final String SQL = "DELETE FROM lists WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL(SQL, new Object[]{orderno});

        final String SQL2 = "DELETE FROM ajs WHERE order_no = ?";
        dbw.execSQL(SQL2, new Object[]{orderno});

        dbw.close();
    }

    public void renameOrder(String orderno, String ajcode, String newAjcode) { //指定回数的指定柜改名
        final String pureaj = getAj(ajcode);
        final String purecode = getCode(newAjcode);
        final String SQL = "UPDATE ajs SET " + pureaj + " = ? WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL(SQL, new Object[]{purecode, orderno});

        dbw.close();
    }

    public String getWeight(String orderno, String aj) { //取得指定回数的指定货柜的柜重
        String result = null;

        SQLiteDatabase dbr = getReadableDatabase();
        try {
            String pureaj = getAj(aj);
            final String SQL = "SELECT " + pureaj + " FROM weights WHERE order_no = ?";
            Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }

            return result;
        } finally {
            dbr.close();
        }
    }

    public String getSeal(String orderno, String aj) { //取得指定回数的指定货柜的封条
        String result = null;

        SQLiteDatabase dbr = getReadableDatabase();
        try {
            String pureaj = getAj(aj);
            final String SQL = "SELECT " + pureaj + " FROM seals WHERE order_no = ?";
            Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }

            return result;
        } finally {
            dbr.close();
        }
    }

    /**
     * 取得指定 回数+ 柜号 的照片
     *
     * @param orderno 回数
     * @param aj      柜号
     * @return 照片的路径（两张照片用逗号分隔），格式如："/sdcard/camera_photos/temp/1578303862961.jpg,/sdcard/camera_photos/temp/34968S7824467.jpg"
     */
    public String getPhoto(String orderno, String aj) {
        String result = null;

        SQLiteDatabase dbr = getReadableDatabase();
        try {
            String pureaj = getAj(aj);
            final String SQL = "SELECT " + pureaj + " FROM photos WHERE order_no = ?";
            Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }

            return result;
        } finally {
            dbr.close();
        }
    }

    public void setWeightSeal(String orderno, String aj, String weight, String seal, String uri, String uri2) {
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            String pureaj = getAj(aj);

            final String SQL = "UPDATE weights SET " + pureaj + " = ? WHERE order_no = ?";
            dbw.execSQL(SQL, new Object[]{weight, orderno});

            final String SQL2 = "UPDATE seals SET " + pureaj + " = ? WHERE order_no = ?";
            dbw.execSQL(SQL2, new Object[]{seal, orderno});

            final String SQL3 = "UPDATE photos SET " + pureaj + " = ? WHERE order_no = ?";
            final String uriconcate = uri + "," + uri2;
            dbw.execSQL(SQL3, new Object[]{uriconcate, orderno});
        } finally {
            dbw.close();
        }
    }

    static boolean backup() {
        boolean result;
        try {
            FileUtils.deleteDir(BACK_DIR);
            FileUtils.createOrExistsDir(BACK_DIR); //如果建立目录失败，后面复制文件时会异常，这里就不检测返回值了
//            FileUtils.createOrExistsDir(PHOTO_DIR); //PHOTO_DIR是在拍照时建立的，此时不需要建立
            String dbbackupfile = Util.Companion.CombinPath(BACK_DIR, BACKUP_FILE);
            result = FileUtils.copyFile(DB_FILE, dbbackupfile, new FileUtils.OnReplaceListener() {
                @Override
                public boolean onReplace() {
                    return true;
                }
            });

            if (FileUtils.isFileExists(PHOTO_DIR)) {
                for (File file : FileUtils.listFilesInDir(PHOTO_DIR)) {
                    String photobackupfile = Util.Companion.CombinPath(BACK_DIR, file.getName());
                    result = FileUtils.copyFile(file.getPath(), photobackupfile, new FileUtils.OnReplaceListener() {
                        @Override
                        public boolean onReplace() {
                            return true;
                        }
                    });
                }
            }

            Util.Companion.ZipFolder(BACK_DIR, BACKUP_ZIP);
//            FileUtils.copyFile(DB_FILE, dbbackupfile, new FileUtils.OnReplaceListener() {
//                @Override
//                public boolean onReplace() {
//                    return true;
//                }
//            });

        } catch (Exception e) {
            result = false;
            Log.e("q-soft", e.getMessage());
        }

        return result;
    }

    static boolean restore() {

//        return FileUtils.copyFile(BACKUP_DATA, DB_FILE, new FileUtils.OnReplaceListener() {
//            @Override
//            public boolean onReplace() {
//                return true;
//            }
//        });

//        String dbbackupfile = Util.Companion.CombinPath(BACK_DIR, BACKUP_FILE);
//        return FileUtils.copyFile(dbbackupfile, DB_FILE, new FileUtils.OnReplaceListener() {
//            @Override
//            public boolean onReplace() {
//                return true;
//            }
//        });

        Boolean bb1 = FileUtils.createOrExistsDir(RESTORE_DIR);
        Boolean bb2 = FileUtils.deleteAllInDir(RESTORE_DIR);
        Boolean b1 = Util.Companion.unpackZip(RESTORE_DIR, BACKUP_ZIP);
        if (b1) {
            String backupdatafilename = FileUtils.getFileName(BACKUP_DATA);
            String backupdatafilefull = Util.Companion.CombinPath(RESTORE_DIR, backupdatafilename);
            Boolean b2 = FileUtils.moveFile(backupdatafilefull, DB_FILE, new FileUtils.OnReplaceListener() {
                @Override
                public boolean onReplace() {
                    return true;
                }
            });

            for (File file : FileUtils.listFilesInDir(RESTORE_DIR)) {
                String photobackupfile = Util.Companion.CombinPath(RESTORE_DIR, file.getName());
                String photodestfile = Util.Companion.CombinPath(PHOTO_DIR, file.getName());
                Boolean b3 = FileUtils.moveFile(photobackupfile, photodestfile, new FileUtils.OnReplaceListener() {
                    @Override
                    public boolean onReplace() {
                        return true;
                    }
                });
                Log.d("q", "restore: " + b3);
                //            result = FileUtils.copyFile(file.getPath(), photobackupfile, new FileUtils.OnReplaceListener() {
                //                @Override
                //                public boolean onReplace() {
                //                    return true;
                //                }
                //            });
            }

            return true;
        } else { //Util.Companion.unpackZip(RESTORE_DIR, BACKUP_ZIP) 失败，有可能是 /sdcard/download/a1-backup.zip 不存在
            return false;
        }
    }

    ///////////////////////////////////////////
    //
    // 以下部分是 临时调货扫描 的功能
    //
    ///////////////////////////////////////////

    public List<String> unplanGetAjs(String orderno) { //获得指定回数（订单）的货柜号清单
        List<String> result = new ArrayList<>();

        final String SQL = "SELECT a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z FROM unplan_ajs WHERE order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});

        try {

            if (cursor.moveToNext()) {
                String a = cursor.getString(0);
                String b = cursor.getString(1);
                String c = cursor.getString(2);
                String d = cursor.getString(3);
                String e = cursor.getString(4);
                String f = cursor.getString(5);
                String g = cursor.getString(6);
                String h = cursor.getString(7);
                String i = cursor.getString(8);
                String j = cursor.getString(9);
                String k = cursor.getString(10) != null ? cursor.getString(10) : "";
                String l = cursor.getString(11) != null ? cursor.getString(11) : "";
                String m = cursor.getString(12) != null ? cursor.getString(12) : "";
                String n = cursor.getString(13) != null ? cursor.getString(13) : "";
                String o = cursor.getString(14) != null ? cursor.getString(14) : "";
                String p = cursor.getString(15) != null ? cursor.getString(15) : "";
                String q = cursor.getString(16) != null ? cursor.getString(16) : "";
                String r = cursor.getString(17) != null ? cursor.getString(17) : "";
                String s = cursor.getString(18) != null ? cursor.getString(18) : "";
                String t = cursor.getString(19) != null ? cursor.getString(19) : "";
                String u = cursor.getString(20) != null ? cursor.getString(20) : "";
                String v = cursor.getString(21) != null ? cursor.getString(21) : "";
                String w = cursor.getString(22) != null ? cursor.getString(22) : "";
                String x = cursor.getString(23) != null ? cursor.getString(23) : "";
                String y = cursor.getString(24) != null ? cursor.getString(24) : "";
                String z = cursor.getString(25) != null ? cursor.getString(25) : "";

                a = String.format("%s-%s", a, "A");
                b = String.format("%s-%s", b, "B");
                c = String.format("%s-%s", c, "C");
                d = String.format("%s-%s", d, "D");
                e = String.format("%s-%s", e, "E");
                f = String.format("%s-%s", f, "F");
                g = String.format("%s-%s", g, "G");
                h = String.format("%s-%s", h, "H");
                i = String.format("%s-%s", i, "I");
                j = String.format("%s-%s", j, "J");
                k = String.format("%s-%s", k, "K");
                l = String.format("%s-%s", l, "L");
                m = String.format("%s-%s", m, "M");
                n = String.format("%s-%s", n, "N");
                o = String.format("%s-%s", o, "O");
                p = String.format("%s-%s", p, "P");
                q = String.format("%s-%s", q, "Q");
                r = String.format("%s-%s", r, "R");
                s = String.format("%s-%s", s, "S");
                t = String.format("%s-%s", t, "T");
                u = String.format("%s-%s", u, "U");
                v = String.format("%s-%s", v, "V");
                w = String.format("%s-%s", w, "W");
                x = String.format("%s-%s", x, "X");
                y = String.format("%s-%s", y, "Y");
                z = String.format("%s-%s", z, "Z");

                result.addAll(Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z));
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    /**
     * 设置柜名（贴在货柜身上的一串编号）
     *
     * @param aj      柜号（A-Z）
     * @param ajcode  柜名（贴在货柜身上的一串编号）
     * @param orderno 临时回数
     */
    public void unplanSetAj(String aj, String ajcode, String orderno) {
        final String SQL = "UPDATE unplan_ajs SET " + aj + " = ? WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();

        try {
            dbw.execSQL(SQL, new Object[]{ajcode, orderno});
        } catch (SQLException e) {
            Log.e("q", e.getMessage());
        }
    }

    /**
     * 列出临时回数
     *
     * @return
     */
    public List<String> unplanGetOrders() {
        List<String> result = new ArrayList();

        final String SQL = "select order_no from unplan_ajs";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{});

        try {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    /**
     * 查询条码指定的产品是否存在
     *
     * @param bar 要查询的条码（从正常回数表中查找，因为是共用条码库）
     * @throws Exception
     */
    public void unplanExists(String bar) throws Exception {
        final String SQL = "SELECT id FROM bars WHERE bar = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{bar});
        if (!cursor.moveToNext()) {
            throw new Exception("没有此产品");
        }
    }

    /**
     * 查询指定条码的产品资料，和装柜信息
     *
     * @param bar     条码
     * @param orderno 临时回数
     * @return
     */
    public BarItem unplanGet(String bar, String orderno) {
        BarItem result = new BarItem();
        result.model = "（没有此产品）";

        final String SQL = "SELECT a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z FROM  unplan_lists WHERE bar = ? AND order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{bar, orderno});
        try {
            if (cursor.moveToNext()) {
                result.a = cursor.getInt(0);
                result.b = cursor.getInt(1);
                result.c = cursor.getInt(2);
                result.d = cursor.getInt(3);
                result.e = cursor.getInt(4);
                result.f = cursor.getInt(5);
                result.g = cursor.getInt(6);
                result.h = cursor.getInt(7);
                result.i = cursor.getInt(8);
                result.j = cursor.getInt(9);
                result.k = cursor.getInt(10);
                result.l = cursor.getInt(11);
                result.m = cursor.getInt(12);
                result.n = cursor.getInt(13);
                result.o = cursor.getInt(14);
                result.p = cursor.getInt(15);
                result.q = cursor.getInt(16);
                result.r = cursor.getInt(17);
                result.s = cursor.getInt(18);
                result.t = cursor.getInt(19);
                result.u = cursor.getInt(20);
                result.v = cursor.getInt(21);
                result.w = cursor.getInt(22);
                result.x = cursor.getInt(23);
                result.y = cursor.getInt(24);
                result.z = cursor.getInt(25);
            }

            final String MODEL_SQL = "SELECT model FROM bars WHERE bar = ?";
            cursor = dbr.rawQuery(MODEL_SQL, new String[]{bar});
            if (cursor.moveToNext()) {
                result.model = cursor.getString(0);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    /**
     * 更新临时调货扫描的装柜表
     *
     * @param bar
     * @param aj
     * @param qty
     * @param orderno
     * @throws Exception
     */
    public void unplanUpdate(String bar, String aj, int qty, String orderno) throws Exception {
        try {
            final String SQL = "SELECT " + aj + " FROM unplan_lists WHERE bar = ? AND order_no = ?";
            SQLiteDatabase dbw = getWritableDatabase();
            Cursor cursor = dbw.rawQuery(SQL, new String[]{bar, orderno});
            if (cursor.getCount() > 0) {
                final String UPDATE_SQL = "UPDATE unplan_lists SET " + aj + " = " + aj + " + ? WHERE bar = ? AND order_no = ?";
                dbw.execSQL(UPDATE_SQL, new Object[]{qty, bar, orderno});
            } else {
                final String GET_SQL = "SELECT model FROM bars WHERE bar = ?";
                cursor = dbw.rawQuery(GET_SQL, new String[]{bar});
                cursor.moveToNext(); //因为程序在之前已经检查过条码是有资料的，所以这里应该不会出错
                String model = cursor.getString(0);

                final String INSERT_SQL = "INSERT INTO unplan_lists (bar, model, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z, order_no) VALUES(?,?,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,?);";
                dbw.execSQL(INSERT_SQL, new Object[]{bar, model, orderno});

                final String UPDATE_SQL = "UPDATE unplan_lists SET " + aj + " = " + aj + " + ? WHERE bar = ? AND order_no = ?";
                dbw.execSQL(UPDATE_SQL, new Object[]{qty, bar, orderno});
            }

            dbw.close();

            unplanRetime(bar, aj, orderno); //更新最后一次扫描时间
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        }
    }

    /**
     * 更新暂时调货扫描的最后一次扫描时间
     *
     * @param bar
     * @param aj
     * @param orderno
     */
    private void unplanRetime(String bar, String aj, String orderno) { //更新 回数+条码+柜号（aj） 的最后一次扫描时间
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            final String SQL = "SELECT id FROM unplan_times WHERE order_no = ? AND bar = ?";
            Cursor cursor = dbw.rawQuery(SQL, new String[]{orderno, bar});
            if (cursor.getCount() > 0) { //如果已经有记录，更新
                final String SQL2 = "UPDATE unplan_times SET " + aj + " = CURRENT_TIMESTAMP, allaj = CURRENT_TIMESTAMP  WHERE order_no = ? AND bar = ?";
                dbw.execSQL(SQL2, new Object[]{orderno, bar});

            } else { //如果没有记录，插入

                final String SQL3 = "INSERT INTO unplan_times (order_no, bar, " + aj + " , allaj, " + aj + "_mark) VALUES (?, ?, CURRENT_TIMESTAMP,CURRENT_TIMESTAMP, 0)";
                dbw.execSQL(SQL3, new Object[]{orderno, bar});
            }
            cursor.close();
        } finally {
            dbw.close();
        }
    }

    /**
     * 获取当前临时订单的所有产品的已出货数量
     *
     * @param orderno
     * @return
     */
    public int unplanGetChecked(String orderno) {
        int result = 0;

        final String SQL = "SELECT SUM(a+b+c+d+e+f+g+h+i+j+k+l+m+n+o+p+q+r+s+t+u+v+w+x+y+z) FROM unplan_lists WHERE order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});

        try {
            while (cursor.moveToNext()) {
                result += cursor.getInt(0);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    /**
     * 取得一个临时回数的code-aj，对于纯aj不取（纯aj指aj柜号，而不是柜名的项目），因纯aj肯定是没有装过的
     *
     * @param orderno
     * @return
     */
    public List<String> unplanGetAjOfOrder(String orderno) {
        List<String> result = new ArrayList<>();

        final String SQL = "SELECT a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z FROM unplan_ajs WHERE order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
        if (cursor.moveToNext()) {
            String a = cursor.getString(0);
            String b = cursor.getString(1);
            String c = cursor.getString(2);
            String d = cursor.getString(3);
            String e = cursor.getString(4);
            String f = cursor.getString(5);
            String g = cursor.getString(6);
            String h = cursor.getString(7);
            String i = cursor.getString(8);
            String j = cursor.getString(9);
            String k = cursor.getString(10) != null ? cursor.getString(10) : "";
            String l = cursor.getString(11) != null ? cursor.getString(11) : "";
            String m = cursor.getString(12) != null ? cursor.getString(12) : "";
            String n = cursor.getString(13) != null ? cursor.getString(13) : "";
            String o = cursor.getString(14) != null ? cursor.getString(14) : "";
            String p = cursor.getString(15) != null ? cursor.getString(15) : "";
            String q = cursor.getString(16) != null ? cursor.getString(16) : "";
            String r = cursor.getString(17) != null ? cursor.getString(17) : "";
            String s = cursor.getString(18) != null ? cursor.getString(18) : "";
            String t = cursor.getString(19) != null ? cursor.getString(19) : "";
            String u = cursor.getString(20) != null ? cursor.getString(20) : "";
            String v = cursor.getString(21) != null ? cursor.getString(21) : "";
            String w = cursor.getString(22) != null ? cursor.getString(22) : "";
            String x = cursor.getString(23) != null ? cursor.getString(23) : "";
            String y = cursor.getString(24) != null ? cursor.getString(24) : "";
            String z = cursor.getString(25) != null ? cursor.getString(25) : "";

            Pattern patt = Pattern.compile("[A-Z]{2,6}[0-9]{5,10}");
            if (patt.matcher(a).matches()) {
                result.add(a + "-A");
            }
            if (patt.matcher(b).matches()) {
                result.add(b + "-B");
            }
            if (patt.matcher(c).matches()) {
                result.add(c + "-C");
            }
            if (patt.matcher(d).matches()) {
                result.add(d + "-D");
            }
            if (patt.matcher(e).matches()) {
                result.add(e + "-E");
            }
            if (patt.matcher(f).matches()) {
                result.add(f + "-F");
            }
            if (patt.matcher(g).matches()) {
                result.add(g + "-G");
            }
            if (patt.matcher(h).matches()) {
                result.add(h + "-H");
            }
            if (patt.matcher(i).matches()) {
                result.add(i + "-I");
            }
            if (patt.matcher(j).matches()) {
                result.add(j + "-J");
            }
            if (k != null && patt.matcher(k).matches()) {
                result.add(k + "-K");
            }
            if (l != null && patt.matcher(l).matches()) {
                result.add(l + "-L");
            }
            if (m != null && patt.matcher(m).matches()) {
                result.add(m + "-M");
            }
            if (n != null && patt.matcher(n).matches()) {
                result.add(n + "-N");
            }
            if (o != null && patt.matcher(o).matches()) {
                result.add(o + "-O");
            }
            if (p != null && patt.matcher(p).matches()) {
                result.add(p + "-P");
            }
            if (q != null && patt.matcher(q).matches()) {
                result.add(q + "-Q");
            }
            if (r != null && patt.matcher(r).matches()) {
                result.add(r + "-R");
            }
            if (s != null && patt.matcher(s).matches()) {
                result.add(s + "-S");
            }
            if (t != null && patt.matcher(t).matches()) {
                result.add(t + "-T");
            }
            if (u != null && patt.matcher(u).matches()) {
                result.add(u + "-U");
            }
            if (v != null && patt.matcher(v).matches()) {
                result.add(v + "-V");
            }
            if (w != null && patt.matcher(w).matches()) {
                result.add(w + "-W");
            }
            if (x != null && patt.matcher(x).matches()) {
                result.add(x + "-X");
            }
            if (y != null && patt.matcher(y).matches()) {
                result.add(y + "-Y");
            }
            if (z != null && patt.matcher(z).matches()) {
                result.add(z + "-Z");
            }
        }

        return result;
    }

    /**
     * 重置临时调货扫描的指定的临时回数。
     *
     * @param orderno
     */
    public void unplanResetOrder(String orderno) {
        //因临时回数固定为10个，所以不应删除，而是将柜名和数量重置
        final String SQL = "UPDATE unplan_ajs SET a = '', b='', c='', d='', e='', f='', g='',h ='',i ='', j='', k='',l ='', m='', n='', o='', p='', q='', r='', s='', t='', u='', v='', w='', x='', y='', z='' WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            dbw.execSQL(SQL, new Object[]{orderno});

            final String SQL2 = "DELETE FROM unplan_lists WHERE order_no = ?";
            dbw.execSQL(SQL2, new Object[]{orderno});

            final String SQL3 = "DELETE FROM unplan_weights WHERE order_no = ?";
            dbw.execSQL(SQL3, new Object[]{orderno});

            final String SQL4 = "DELETE FROM unplan_seals WHERE order_no = ?";
            dbw.execSQL(SQL4, new Object[]{orderno});

            final String SQL5 = "DELETE FROM unplan_photos WHERE order_no = ?";
            dbw.execSQL(SQL5, new Object[]{orderno});

            final String SQL6 = "DELETE FROM unplan_times WHERE order_no = ?";
            dbw.execSQL(SQL6, new Object[]{orderno});
        } finally {
            dbw.close();
        }
    }

    /**
     * 查询出指定临时订单号（orderno）+柜号（aj）的的装柜情况
     *
     * @param orderno
     * @param aj
     * @return
     */
    public List<BarItemOfAj> unplanGetStatsOfAj(String orderno, String aj) {
        List<BarItemOfAj> result = new ArrayList<BarItemOfAj>();

        final String pureaj = getAj(aj);

        final String SQL = "SELECT t1.model, t1." + pureaj + ", t1.a+t1.b+t1.c+t1.d+t1.e+t1.f+t1.g+t1.h+t1.i+t1.j+t1.k+t1.l+t1.m+t1.n+t1.o+t1.p+t1.q+t1.r+t1.s+t1.t+t1.u+t1.v+t1.w+t1.x+t1.y+t1.z, t2." + pureaj + "_mark FROM unplan_lists t1 INNER JOIN unplan_times t2 ON t1.order_no = t2.order_no AND t1.bar = t2.bar  WHERE t1.order_no = ? AND t1." + pureaj + " <> 0  ORDER BY t2." + pureaj;
        String sql = SQL;
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(sql, new String[]{orderno});

        try {
            while (cursor.moveToNext()) {
                BarItemOfAj bi = new BarItemOfAj();
                bi.model = cursor.getString(0);
                bi.done = cursor.getInt(1); //本柜装数
                bi.otheraj = cursor.getInt(2) - bi.done; //其他柜的装数 = 所有柜的总装数 - 本柜装数
                bi.mark = cursor.getInt(3); //是否标记为黄色，0表示未标记，1表示标记
                result.add(bi);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    /**
     * 标记或取消标记临时调货扫描的 型号+回数+柜号（aj），本来想用 条码+回数+柜号（aj），但UI中没有显示条码，所以用型号代替
     *
     * @param model
     * @param aj
     * @param orderno
     * @param mark
     */
    public void unplanMark(String model, String aj, String orderno, Integer mark) {
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            final String bar = unplanGetBarOfModel(model);
            final String pureaj = getAj(aj);
            final String SQL = "UPDATE unplan_times SET " + pureaj + "_mark = ? WHERE order_no = ? AND bar = ?";
            dbw.execSQL(SQL, new Object[]{mark, orderno, bar});
        } finally {
            dbw.close();
        }
    }

    /**
     * 获得临时调货扫描的指定型号的条码
     *
     * @param model
     * @return
     */
    public String unplanGetBarOfModel(String model) {
        String result = "";
        SQLiteDatabase dbr = getReadableDatabase();
        final String SQL = "SELECT bar FROM unplan_lists WHERE model = ?";
        Cursor cursor = dbr.rawQuery(SQL, new String[]{model});
        if (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        return result;
    }

    /**
     * 取得临时调货扫描的指定回数的指定货柜的柜重
     * String result = null;
     *
     * @param orderno
     * @param aj
     * @return
     */
    public String unplanGetWeight(String orderno, String aj) {
        String result = "";

        SQLiteDatabase dbr = getReadableDatabase();
        try {
            String pureaj = getAj(aj);
            final String SQL = "SELECT " + pureaj + " FROM unplan_weights WHERE order_no = ?";
            Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }

            return result;
        } finally {
            dbr.close();
        }
    }

    /**
     * 取得临时调货扫描的指定回数的指定货柜的封条
     *
     * @param orderno
     * @param aj
     * @return
     */
    public String unplanGetSeal(String orderno, String aj) {
        String result = "";

        SQLiteDatabase dbr = getReadableDatabase();
        try {
            String pureaj = getAj(aj);
            final String SQL = "SELECT " + pureaj + " FROM unplan_seals WHERE order_no = ?";
            Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }

            return result;
        } finally {
            dbr.close();
        }
    }

    /**
     * 取得临时调货扫描指定 回数+ 柜号 的照片
     *
     * @param orderno 回数
     * @param aj      柜号
     * @return 照片的路径（两张照片用逗号分隔），格式如："/sdcard/camera_photos/temp/1578303862961.jpg,/sdcard/camera_photos/temp/34968S7824467.jpg"
     */
    public String unplanGetPhoto(String orderno, String aj) {
        String result = "";

        SQLiteDatabase dbr = getReadableDatabase();
        try {
            String pureaj = getAj(aj);
            final String SQL = "SELECT " + pureaj + " FROM unplan_photos WHERE order_no = ?";
            Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }

            return result;
        } finally {
            dbr.close();
        }
    }

    /**
     * 删除临时调货扫描的指定回数（orderno)的指定柜号（ajcode）
     *
     * @param orderno
     * @param ajcode  FIXME：柜重 和 封条 好像没有清除
     */
    public void unplanDelAj(String orderno, String ajcode) {
        final String pureaj = getAj(ajcode);
        final String SQL = "UPDATE unplan_lists SET " + pureaj + " = 0 WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL(SQL, new Object[]{orderno});

        final String SQL2 = "UPDATE unplan_ajs SET " + pureaj + " = '' WHERE order_no = ?";
        dbw.execSQL(SQL2, new Object[]{orderno});

        final String SQL3 = "UPDATE unplan_times SET  " + pureaj + " = null, " + pureaj + "_mark = 0 WHERE order_no = ? "; //清除扫描时间与加黄标记
        dbw.execSQL(SQL3, new Object[]{orderno});

        final String SQL4 = "UPDATE unplan_photos SET " + pureaj + " = '' WHERE order_no = ?"; //清除照片
        dbw.execSQL(SQL4, new Object[]{orderno});

        dbw.close();
    }

    /**
     * 指定临时调货扫描的回数的指定柜改名
     *
     * @param orderno
     * @param ajcode
     * @param newAjcode
     */
    public void unplanRenameOrder(String orderno, String ajcode, String newAjcode) {
        final String pureaj = getAj(ajcode);
        final String purecode = getCode(newAjcode);
        final String SQL = "UPDATE unplan_ajs SET " + pureaj + " = ? WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL(SQL, new Object[]{purecode, orderno});

        dbw.close();
    }

    /**
     * 设置临时调货扫描的回数别名、柜重、封条
     *
     * @param orderno
     * @param aj
     * @param alias
     * @param weight
     * @param seal
     * @param uri
     * @param uri2
     */
    public void unplanSetAliasWeightSeal(String orderno, String aj, String alias, String weight, String seal, String uri, String uri2) {
        SQLiteDatabase dbw = getWritableDatabase();
        try {
            String pureaj = getAj(aj);

//            final String SQL = "UPDATE unplan_weights SET " + pureaj + " = ? WHERE order_no = ?";
//            dbw.execSQL(SQL, new Object[]{weight, orderno});

            final String SQL = "UPDATE unplan_order_aliases SET alias = ? WHERE order_no = ?";
            dbw.execSQL(SQL, new Object[]{alias, orderno});

            final ContentValues weight_contents = new ContentValues();
            weight_contents.put("order_no", orderno);
            weight_contents.put(pureaj, weight);
            if (dbw.update("unplan_weights", weight_contents, "order_no=?", new String[]{orderno}) == 0) {
                final ContentValues contents = new ContentValues();
                contents.put("order_no", orderno);
                contents.put(pureaj, weight);
                dbw.insert("unplan_weights", null, contents);
            }

//            final String SQL2 = "UPDATE unplan_seals SET " + pureaj + " = ? WHERE order_no = ?";
//            dbw.execSQL(SQL2, new Object[]{seal, orderno});

            final ContentValues seal_contents = new ContentValues();
            seal_contents.put("order_no", orderno);
            seal_contents.put(pureaj, seal);
            if (dbw.update("unplan_seals", seal_contents, "order_no=?", new String[]{orderno}) == 0) {
                final ContentValues contents = new ContentValues();
                contents.put("order_no", orderno);
                contents.put(pureaj, seal);
                dbw.insert("unplan_seals", null, contents);
            }

//            final String SQL3 = "UPDATE unplan_photos SET " + pureaj + " = ? WHERE order_no = ?";
//            final String uriconcate = uri + "," + uri2;
//            dbw.execSQL(SQL3, new Object[]{uriconcate, orderno});

            final ContentValues photo_contents = new ContentValues();
            photo_contents.put("order_no", orderno);
            photo_contents.put(pureaj, uri + "," + uri2);
            if (dbw.update("unplan_photos", photo_contents, "order_no=?", new String[]{orderno}) == 0) {
                final ContentValues contents = new ContentValues();
                contents.put("order_no", orderno);
                contents.put(pureaj, uri + "," + uri2);
                dbw.insert("unplan_photos", null, contents);
            }
        } finally {
            dbw.close();
        }
    }

    /**
     * 查询出临时调货扫描的指定订单号（orderno）的所有产品的出货情况
     *
     * @param orderno
     * @return
     */
    public List<BarItem> unplanGetStats(String orderno) {
        List<BarItem> result = new ArrayList<BarItem>();

        final String SQL = "SELECT t1.model, t1.a,t1.b,t1.c,t1.d,t1.e,t1.f,t1.g,t1.h,t1.i,t1.j,t1.k,t1.l,t1.m,t1.n,t1.o,t1.p,t1.q,t1.r,t1.s,t1.t,t1.u,t1.v,t1.w,t1.x,t1.y,t1.z  FROM unplan_lists t1 LEFT JOIN unplan_times t2 ON t1.order_no = t2.order_no AND t1.bar = t2.bar WHERE t1.order_no = ?  ORDER BY IFNULL(t2.allaj, '9999-12-31')"; //不用查询 total 和 unchecked，在 BarItem() 里会算出来；只显示装数与计划有差异的行

        final String sql = SQL;

        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(sql, new String[]{orderno});

        try {
            while (cursor.moveToNext()) {
                BarItem bi = new BarItem();
                bi.model = cursor.getString(0);
                bi.a = cursor.getInt(1);
                bi.b = cursor.getInt(2);
                bi.c = cursor.getInt(3);
                bi.d = cursor.getInt(4);
                bi.e = cursor.getInt(5);
                bi.f = cursor.getInt(6);
                bi.g = cursor.getInt(7);
                bi.h = cursor.getInt(8);
                bi.i = cursor.getInt(9);
                bi.j = cursor.getInt(10);
                bi.k = cursor.getInt(11);
                bi.l = cursor.getInt(12);
                bi.m = cursor.getInt(13);
                bi.n = cursor.getInt(14);
                bi.o = cursor.getInt(15);
                bi.p = cursor.getInt(16);
                bi.q = cursor.getInt(17);
                bi.r = cursor.getInt(18);
                bi.s = cursor.getInt(19);
                bi.t = cursor.getInt(20);
                bi.u = cursor.getInt(21);
                bi.v = cursor.getInt(22);
                bi.w = cursor.getInt(23);
                bi.x = cursor.getInt(24);
                bi.y = cursor.getInt(25);
                bi.z = cursor.getInt(26);
                result.add(bi);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }

    /**
     * 修改临时调货扫描的 bar+aj+orderno 指定的数量
     *
     * @param bar
     * @param aj
     * @param qty
     * @param orderno
     * @throws Exception
     */
    public void unplanAlterQty(String bar, String aj, int qty, String orderno) throws Exception {

        if (qty >= 0) {
            final String pureaj = getAj(aj); //UPDATE 时需要用纯aj，而不能是code-aj
            final String SQL = "UPDATE unplan_lists SET " + pureaj + " = ? WHERE bar = ? AND order_no = ?";
            SQLiteDatabase dbw = getWritableDatabase();
            try {
                dbw.execSQL(SQL, new Object[]{qty, bar, orderno});
            } catch (Exception e) {
                throw e;
            } finally {
                dbw.close();
            }
        } else {
            throw new Exception("请不要输入负数");
        }
    }


//    /**
//     * 创建10个临时回数。好像不用创建，因为数据库建立时就已经创建了这些临时回数，在使用过程中只是会删除回数的相关数据，并不会删除回数
//     */
//    private void unplanCreateLists() {
//        SQLiteDatabase dbw = getWritableDatabase();
//        try {
//            String[] unplanLists = {"临时调货-1", "临时调货-2", "临时调货-3", "临时调货-4", "临时调货-5", "临时调货-6", "临时调货-7", "临时调货-8", "临时调货-9", "临时调货-10",};
//            final String SQL = "SELECT order_no FROM unplan_lists WHERE order_no = ?";
//            for (String unplanlist : unplanLists) {
//
//                Cursor cursor = dbw.rawQuery(SQL, new String[]{unplanlist});
//                if (cursor.getCount() == 0) { //如果指定的临时回数没有装柜记录，则删除相关的aj、weight等，在装柜时会重新建立这些记录
//                    final String SQL_INSERT = "INSERT INTO unplan_ajs (order_no,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) VALUES(?,'','','','','','','','','','','','','','','','','','','','','','','','','','');";
//                    dbw.execSQL(SQL_INSERT, new Object[]{unplanlist});
//
//                    dbw.delete(TN_UNPLANAJS, "order_no", unplanLists);
//                    dbw.delete(TN_UNPLANWEIGHTS, "order_no", unplanLists);
//                    dbw.delete(TN_UNPLANSEALS, "order_no", unplanLists);
//                    dbw.delete(TN_UNPLANPHOTOS, "order_no", unplanLists);
//                    dbw.delete(TN_UNPLANTIMES, "order_no", unplanLists);
//                }
//            }
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            dbw.close();
//        }
//    }

    public void importBars(List<String> list) {
        SQLiteDatabase dbw = getWritableDatabase();

        try {
            List<String> bars = new ArrayList<>(); //这是本次收到的回数

            ContentValues cv = new ContentValues();

            for (String line : list) {
                final String[] flds = line.split(",");
                final String bar = flds[0];
                final String model = flds[1];

                cv.clear();
                cv.put(FN_BAR, bar);
                cv.put(FN_MODEL, model);

                dbw.insert(TN_BARS, null, cv);

                Log.d("q", "importList():" + line);
            }

            dbw.close();
        } catch (Exception e) {
            Log.e("q", e.getMessage());
            throw e;
        }
    }

    /**
     * 设置临时回数的别名
     *
     * @param orderno 临时回数的名称
     * @param alias   临时回数的别名
     */
    public void unplanSetOrderAlias(String alias, String orderno) {
        final String SQL = "UPDATE unplan_order_aliases SET alias = ? WHERE order_no = ?";
        SQLiteDatabase dbw = getWritableDatabase();

        try {
            dbw.execSQL(SQL, new Object[]{alias, orderno});
        } catch (SQLException e) {
            Log.e("q", e.getMessage());
        }
    }

    /**
     * 获取临时调货扫描回数的别名
     *
     * @param orderno 临时调货扫描的回数
     * @return 回数的别名
     */
    public String unplanGetOrderAlias(String orderno) {
        String result = "";

        final String SQL = "SELECT alias FROM unplan_order_aliases WHERE order_no = ?";
        SQLiteDatabase dbr = getReadableDatabase();

        Cursor cursor = dbr.rawQuery(SQL, new String[]{orderno});

        try {
            while (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
        } finally {
            cursor.close();
            dbr.close();
        }

        return result;
    }
}
