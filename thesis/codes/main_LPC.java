import java.io.*;
import java.nio.file.Files;

//主程序：LPC-ACC编解码示例及调用
public class main_LPC {
    public static void main(String[] args) throws IOException {
        for (int j = 1; j < 4; j++) {
            int q = j;      //1,2,3,分别对应三种LPC压缩方式（1-经典LPC；2-映射后LPC；3-最佳LPC）
            int num_symbols = (q == 1) ? 511 : 256; //选择误差量的总数
            String[] inputFileArray = {"airplane.raw", "baboon.raw", "cameraman.raw", "lena.raw", "woman_darkhair.raw"};
            for (String s : inputFileArray) {   //对inputFileArray所有文件进行编解码
                String inputFile = "data\\imgs\\" + s;
                File outputFile = new File("result\\LPC_compress_" + q + "_" + s);    //编码压缩输出
                File de_outFile = new File("result\\LPC_decompress_of_" + q + "_" + s);   //解码输出
                File weightFile = new File("data\\weight\\" + s + "_weight.txt"); //最佳权值文件路径

//                编码
                LPC_compress lpc_compress = new LPC_compress(num_symbols);  //实例化最佳线性预测编码类
                try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(Files.newOutputStream(outputFile.toPath())))) {
                    double[] a = lpc_compress.get_weight(q, weightFile);    //获得所需的编码权值
                    lpc_compress.compress(inputFile, a, out, q);    //编码
                }

//                解码
                LPC_decompress lpc_decompress = new LPC_decompress(num_symbols);    //解码类同编码
                try (BitInputStream in = new BitInputStream(new BufferedInputStream(Files.newInputStream(outputFile.toPath()))); OutputStream out = new BufferedOutputStream(Files.newOutputStream(de_outFile.toPath()))) {
                    double[] a = lpc_decompress.get_weight(q, weightFile);
                    lpc_decompress.decompress(in, a, out, q);
                }
            }
        }

    }
}