import java.io.*;

/**
 * 非自适应性编码类
 * 变量：1、编码符号数；2、频率折半阈值；3、初始频率表；4、统计后的频率表
 * 方法：
 * 1、构造方法：传入参数值，实例化化相关类
 * 2、statistic：输入参数：输入文件流；输出：无。统计符号频率，按阈值要求新建符号频率表
 * 3、compress：输入参数：输入文件流，压缩输出文件流；输出：无。使用statistic统计好的频率表，进行算术码压缩
 */

public class ArithmeticCompress {
    int num_symbols;
    int threshold_value;
    FlatFrequencyTable initFreqs;
    FrequencyTable freqs;

    ArithmeticCompress(int num_symbol, int threshold_value) {
        this.num_symbols = num_symbol;
        this.threshold_value = threshold_value;
        this.initFreqs = new FlatFrequencyTable(this.num_symbols);
        this.freqs = new SimpleFrequencyTable(this.initFreqs);
    }

    //全局概率统计方法
    public void statistic(InputStream in) throws IOException {
        in.mark(in.available()+1);
        while (true) {
            int symbol = in.read();
            if (symbol == -1)
                break;
            symbol = symbol - 65;
            if (symbol < 0)
                symbol = 26;
            this.freqs.increment(symbol);
//            if (this.freqs.getTotal() >= this.threshold_value) {
//                for (int i = 0; i < this.freqs.getSymbolLimit(); i++) {
//                    this.freqs.set(i, (this.freqs.get(i) + 1) / 2);
//                }
//            }
        }
    }

    //保存概率表方法
    public void writeFreqs(BitOutputStream out) throws IOException {
        for(int i=0;i<this.freqs.getSymbolLimit();i++){
            for(int j=0;j<32;j++){
                int temp_bit=this.freqs.get(i)>>j&1;
                out.write(temp_bit);
            }
        }
    }

    public void compress(InputStream in,BitOutputStream out) throws IOException {
        ArithmeticEncoder enc = new ArithmeticEncoder(32, out);
        //压缩
        in.reset();
        while (true) {
            int symbol = in.read();
            if (symbol == -1)
                break;
            symbol=symbol-65;
            if(symbol<0)
                symbol=26;
            enc.write(this.freqs, symbol);
        }
        enc.write(this.freqs, this.num_symbols-1);  // EOF
        enc.finish();  // Flush remaining code bits
    }
}
