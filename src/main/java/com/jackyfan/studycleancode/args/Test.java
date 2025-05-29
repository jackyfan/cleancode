package com.jackyfan.studycleancode.args;

public class Test {
    public static void main(String[] args) {
        try {
            Args arg = new Args("l,p#,d*", args);
            boolean logging = arg.getBoolean('l');
            int port = arg.getInt('p');
            String directory = arg.getString('d');
            //executeApplication(logging, port, directory);
            System.out.printf("logging:%s,port:%d,directory:%s",logging,port,directory);
        } catch (ArgsException e) {
            System.out.printf("Argumenterror:%s\n", e.errorMessage());
        }
    }
}
