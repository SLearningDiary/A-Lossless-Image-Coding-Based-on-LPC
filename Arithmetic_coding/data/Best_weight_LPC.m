%% 1、清除缓存
% clear;clc;
%% 2、读入.raw文件数据，并将数据转换为512*512的像素信息矩阵
row=512;column=512;     %行和列
num=column*row;     %数据总数

filename='imgs\\airplane.raw';
% filename='imgs\\baboon.raw';
% filename='imgs\\cameraman.raw';
% filename='imgs\\lena.raw';
% filename='imgs\\woman.raw';
fileID=fopen(filename,'r');   %只读的方式打开指定的文件
data=fread(fileID,num);  %读入所有数据，默认为double数据类型
fclose(fileID);     %关闭文件传输流
data=reshape(data,[row,column])';   %重新定义数据矩阵的大小为row*column
%% 3、统计矩阵R的值
x=zeros(1,4);       %周围像素点x1~x4矩阵
R_left_sum=zeros(4); %最佳线性系数方程的右侧矩阵
R_right_sum=zeros(4,1);  %左侧矩阵
for i=1:row
    for j=1:column
        x0=data(i,j);
        if(j==1)
            x(1)=0;
        else
            x(1)=data(i,j-1);
        end
        if(j==1||i==1)
            x(2)=0;
        else
            x(2)=data(i-1,j-1);
        end
        if(i==1)
            x(3)=0;
        else
            x(3)=data(i-1,j);
        end
        if(j==row||i==1)
            x(4)=0;
        else
            x(4)=data(i-1,j+1);
        end
        R_left_sum=R_left_sum+x.*x';
        R_right_sum=R_right_sum+x'*x0;
    end
end
%% 4、计算最佳线性预测权值
R_left=R_left_sum/num;
R_right=R_right_sum/num;
a=R_left\R_right     %求左逆矩阵与右矩阵的左乘，A\B=inv(A)*B，但比速度与精度都优于后者
%% 5、保存最佳线性预测权值到.txt文件
weightFileName=['weight\\',filename(7:length(filename)),'_weight.txt'];
fid=fopen(weightFileName,'w');
fprintf(fid,'%.4f\n',a);      %每一个数据(4位小数)单独为一行，以便于提取时区分数据
fclose(fid);
