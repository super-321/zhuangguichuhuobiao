package cn.scanshop.www.a1;

public class BarItemOfAj { //特定一个 回数+货柜 之下的统计项
    public String ord;
    public String model;

    public int planning; //计划数量
    public int done; //已装数量

    public int otheraj = 0; //其他柜的已装数量。统计指定的一个柜的差异数量时，需要扣除其他柜的已装数量

    public int getUnchecked() {
        return planning - done - otheraj;
    }

    public int mark; //是否标记为黄色，0表示未标记，1表示标记
}