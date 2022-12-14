import java.io.*;

//LPC-ACC解码类
public class LPC_decompress {    //LPC:linear predictive coding  线性预测编码类
    int num_symbols;        //编码符号数，多一个终止符号

    LPC_decompress(int num_symbol) {
        this.num_symbols = num_symbol;
    }

    //    解码
    public void decompress(BitInputStream in, double[] a, OutputStream out, int q) throws IOException {
        FlatFrequencyTable initFreqs = new FlatFrequencyTable(this.num_symbols);
        FrequencyTable freqs = new SimpleFrequencyTable(initFreqs);
        ArithmeticDecoder dec = new ArithmeticDecoder(32, in);
        int num_index = 512;    //待编码数据矩阵边长
        int[][] pixel_array = new int[num_index][num_index]; //512*512
        double[] x = new double[5]; //像素值x0~x4
        int e, E;  //误差量，e为初始误差，用于恢复真实像素；E为映射后的误差，也是频率表统计对象
        for (int row = 0; row < num_index; row++) {
            for (int column = 0; column < num_index; column++) {
                E = dec.read(freqs);
                x[1] = (column == 0) ? 0 : pixel_array[row][column - 1];
                x[2] = (column == 0 || row == 0) ? 0 : pixel_array[row - 1][column - 1];
                x[3] = (row == 0) ? 0 : pixel_array[row - 1][column];
                x[4] = (column == num_index - 1 || row == 0) ? 0 : pixel_array[row - 1][column + 1];
                x[0] = x[1] * a[0] + x[2] * a[1] + x[3] * a[2] + x[4] * a[3];
                e = (q == 1) ? E - 255 : inverse_mapping(E, (int) x[0]);  //根据题号选择映射规则，映射LPC中：将预测值取整后，再参与逆映射，以保证映射和逆映射一致，实现无失真传输
                pixel_array[row][column] = e + (int) x[0];
                out.write(pixel_array[row][column]);
                freqs.increment(E); //在频率表统计的仍为映射后，缩小范围的E；求逆映射仅为求解出原像素值
            }
        }
    }

    //    权重读取
    public double[] get_weight(int sel, File weightFile) throws IOException {   //获得预测权值
        double[] a = new double[4];
        if (sel == 3) {     //最佳LPC需要从外部文件读入权值数据
            try (FileInputStream weightStream = new FileInputStream(weightFile);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(weightStream))) {
                for (int i = 0; i < 4; i++) {
                    a[i] = Double.parseDouble(bufferedReader.readLine());
                }
            }
        } else {   //非最佳LPC权值均为0.25
            for (int i = 0; i < 4; i++) {
                a[i] = 0.25;
            }
        }
        return a;
    }

    //    误差逆映射
    public int inverse_mapping(int E, int x) {   //逆映射函数
        int e = 0;
        if (x >= 0 && x < 128) {
            if (E <= 2 * x + 1)
                e = (E % 2 == 0) ? E / (-2) : (E + 1) / 2;
            if (E > 2 * x + 1)
                e = E - x;
        } else {
            if (E <= 511 - 2 * x)
                e = (E % 2 == 0) ? E / 2 : -(E + 1) / 2;
            if (E > 511 - 2 * x)
                e = 255 - E - x;
        }
        return e;
    }
}
