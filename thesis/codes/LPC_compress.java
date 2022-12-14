import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

//LPC-ACC编码类
public class LPC_compress {    //LPC:linear predictive coding  线性预测编码类
    int num_symbols;        //编码符号数，多一个终止符号

    LPC_compress(int num_symbol) {
        this.num_symbols = num_symbol;
    }

    //    编码
    public void compress(String inputFile, double[] a, BitOutputStream out, int q) throws IOException { //编码方法
        FlatFrequencyTable initFreqs = new FlatFrequencyTable(this.num_symbols);
        FrequencyTable freqs = new SimpleFrequencyTable(initFreqs);
        ArithmeticEncoder enc = new ArithmeticEncoder(32, out);
        int num_index = 512;    //待编码数据矩阵边长
        int[] x = new int[5]; //像素值x0~x4
        int e, E;  //误差量，e为初始误差，用于恢复真实像素；E为映射后的误差，也是频率表统计对象
        byte[] pixel_array = Files.readAllBytes(Paths.get(inputFile));      //一次性读入全部图像数据
        for (int i = 0; i < pixel_array.length; i++) {  //遍历编码
            x[1] = (i % num_index == 0) ? 0 : (pixel_array[i - 1] & 0xff);
            x[2] = (i % num_index == 0 || i < num_index) ? 0 : (pixel_array[i - num_index - 1] & 0xff);
            x[3] = (i < num_index) ? 0 : (pixel_array[i - num_index] & 0xff);
            x[4] = (i % num_index == num_index - 1 || i < num_index) ? 0 : (pixel_array[i - num_index + 1] & 0xff);
            x[0] = (int) (x[1] * a[0] + x[2] * a[1] + x[3] * a[2] + x[4] * a[3]);
            e = (pixel_array[i] & 0xff) - x[0];     //计算初始预测误差
            E = (q == 1) ? e + 255 : mapping(e, x[0]); //根据题号选择映射规则，映射LPC中：将预测值取整后，再参与映射，以保证映射和逆映射一致，实现无失真传输
            enc.write(freqs, E);
            freqs.increment(E);
        }
    }

    //    权重读取
    public double[] get_weight(int sel, File weightFile) throws IOException {   //获得预测权值
        double[] a = new double[4];     //权值数组
        if (sel == 3) {     //最佳LPC需要从外部文件读入权值数据
            try (FileInputStream weightStream = new FileInputStream(weightFile);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(weightStream))) {
                for (int i = 0; i < 4; i++) {   //按行获得权值信息，并幅值给a数组
                    a[i] = Double.parseDouble(bufferedReader.readLine());
                }
            }
        } else {    //非最佳LPC权值均为0.25
            for (int i = 0; i < 4; i++) {
                a[i] = 0.25;
            }
        }
        return a;
    }

    //    误差映射
    public int mapping(int e, int x) {   //映射函数
        int E = 0;
        if (x >= 0 && x < 128) {
            if (e <= 0)
                E = -2 * e;
            if (e > 0 && e <= x + 1)
                E = 2 * e - 1;
            if (e > x + 1)
                E = e + x;
        } else {
            if (e >= 0)
                E = 2 * e;
            if (e < 0 && e >= x - 256)
                E = -2 * e - 1;
            if (e < x - 256)
                E = -(e + x - 255);
        }
        return E;
    }
}