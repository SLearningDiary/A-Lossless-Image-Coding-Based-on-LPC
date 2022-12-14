%% 1���������
% clear;clc;
%% 2������.raw�ļ����ݣ���������ת��Ϊ512*512��������Ϣ����
row=512;column=512;     %�к���
num=column*row;     %��������

filename='imgs\\airplane.raw';
% filename='imgs\\baboon.raw';
% filename='imgs\\cameraman.raw';
% filename='imgs\\lena.raw';
% filename='imgs\\woman.raw';
fileID=fopen(filename,'r');   %ֻ���ķ�ʽ��ָ�����ļ�
data=fread(fileID,num);  %�����������ݣ�Ĭ��Ϊdouble��������
fclose(fileID);     %�ر��ļ�������
data=reshape(data,[row,column])';   %���¶������ݾ���Ĵ�СΪrow*column
%% 3��ͳ�ƾ���R��ֵ
x=zeros(1,4);       %��Χ���ص�x1~x4����
R_left_sum=zeros(4); %�������ϵ�����̵��Ҳ����
R_right_sum=zeros(4,1);  %������
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
%% 4�������������Ԥ��Ȩֵ
R_left=R_left_sum/num;
R_right=R_right_sum/num;
a=R_left\R_right     %������������Ҿ������ˣ�A\B=inv(A)*B�������ٶ��뾫�ȶ����ں���
%% 5�������������Ԥ��Ȩֵ��.txt�ļ�
weightFileName=['weight\\',filename(7:length(filename)),'_weight.txt'];
fid=fopen(weightFileName,'w');
fprintf(fid,'%.4f\n',a);      %ÿһ������(4λС��)����Ϊһ�У��Ա�����ȡʱ��������
fclose(fid);
