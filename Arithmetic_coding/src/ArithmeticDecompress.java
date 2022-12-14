import java.io.*;

/**
 * 非自适应性解码类
 * 变量：1、编码符号数；2、频率折半阈值；3、初始频率表；4、统计后的频率表
 * 方法：
 * 1、构造方法：传入参数值，实例化化相关类
 * 2、statistic：输入参数：输入文件流；输出：无。统计符号频率，按阈值要求新建符号频率表
 * 3、compress：输入参数：输入文件流，压缩输出文件流；输出：无。使用statistic统计好的频率表，进行算术码压缩
 */

public class ArithmeticDecompress {
    int num_symbols;
    int threshold_value;
    FlatFrequencyTable initFreqs;
    FrequencyTable freqs;

    ArithmeticDecompress(int num_symbol, int threshold_value) {
        this.num_symbols = num_symbol;
        this.threshold_value = threshold_value;
        this.initFreqs = new FlatFrequencyTable(this.num_symbols);
        this.freqs = new SimpleFrequencyTable(this.initFreqs);
    }

    //读入并恢复全局概率表方法
    public void readFreqs(BitInputStream in) throws IOException {
        for (int i = 0; i < this.freqs.getSymbolLimit(); i++) {
            String temp="";
            for (int j = 0; j <32 ; j++) {
                temp=in.read()+temp;
            }
            this.freqs.set(i,Integer.parseInt(temp,2));
        }
    }

    public void decompress(BitInputStream in,OutputStream out) throws IOException {
        ArithmeticDecoder dec = new ArithmeticDecoder(32, in);
        while (true) {
            int symbol = dec.read(this.freqs);
            if (symbol == this.num_symbols-1)  // EOF symbol
                break;
            if(symbol==this.num_symbols-2)
                out.write(32);
            else
                out.write(symbol+65);
        }
    }
}
