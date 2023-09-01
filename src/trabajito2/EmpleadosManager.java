package trabajito2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

public class EmpleadosManager {
    private RandomAccessFile rcods, remps;
    public EmpleadosManager(){
        try{
            File f = new File("company");
            f.mkdir();
            
            rcods = new RandomAccessFile("company/codigos.emp","rw");
            remps = new RandomAccessFile("company/empleados.emp","rw");
            
            initCode();
        }catch(IOException e){
            System.out.println("XD, kkkkkkkkkkkkkkkkkkkk I LOVE POLAND I LOVE POLAND");
        }
    }
    
    private void initCode() throws IOException{
        if(rcods.length()==0){
            rcods.writeInt(1);
        }
    }
    private int getCode()throws IOException{
        rcods.seek(0);
        int code = rcods.readInt();
        rcods.seek(0);
        rcods.writeInt(code+1);
        return code;
    }
    public void addEmployee(String name, double salary)throws IOException{
        remps.seek(remps.length());
        int code =  getCode();
        remps.writeInt(code);
        remps.writeUTF(name);
        remps.writeDouble(salary);
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        remps.writeLong(0);
        
        createEmployeeFolders(code);
    }
    private String employeeFolder(int code){
        return "company/empleado"+code;
    }
    private void createEmployeeFolders(int code) throws IOException{
        File edir =  new File(employeeFolder(code));
        edir.mkdir();
        
        createYearSalesFileFor(code);
    }
    
    private RandomAccessFile salesFileFor(int code) throws IOException{
        String dirPadre = employeeFolder(code);
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        String path = dirPadre+"/ventas"+yearActual+".emp";
        return new RandomAccessFile(path,"rw");
    }
    
    private void createYearSalesFileFor(int code) throws IOException{
        RandomAccessFile ryear = salesFileFor(code);
        if(ryear.length()==0){
            for(int m=0;m<12;m++){
                ryear.writeDouble(0);
                ryear.writeBoolean(false);
            }
        }
    }
    
    public String imprimirEmpleados() throws IOException{
        long finish = remps.length();
        String lista = ".";
        rcods.seek(0);
        while(remps.getFilePointer()<finish){
            int code = remps.readInt();
            String name = remps.readUTF();
            double salary = remps.readDouble();
            Date date = new Date(remps.readLong());
            if(remps.readLong()==0){
                lista += "Codigo: "+code+" - Nombre: "+name+" - Salario: "+salary+" - Fecha de Contratacion: "+date+"\n";
            }
        }
        return lista;
    }
    
    private   boolean    isEmployeeActive(int    code)   throws    IOException{
        remps.seek(0);
        while(remps.getFilePointer()<remps.length()){
            int cod = remps.readInt();
            long pos = remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);
            if(remps.readLong()==0 && cod==code){
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }
    
    public boolean fireEmployee(int code) throws IOException{
        if(isEmployeeActive(code)){
            String name = remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Despidiendo a: "+name);
            return true;
        }
        return false;
    }
    
    public void addSaleToEmployee(int code, double sale) throws IOException {
        if(isEmployeeActive(code)) {
            RandomAccessFile sales = salesFileFor(code);
            int pos = Calendar.getInstance().get(Calendar.MONTH) * 9;
            sales.seek(pos);
            double monto = sales.readDouble() + sale;
            sales.seek(pos);
            sales.writeDouble(monto);
        }
    }
    
    public void payEmployee(int code) throws IOException {
        double sal = 0;
        if (isEmployeeActive(code) && !isEmployeePayed(code)) {
            RandomAccessFile sales = salesFileFor(code);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH);
            int pos = month * 9;
            sales.seek(pos);
            String nombre = remps.readUTF();
            sal = remps.readDouble();
            double ventas = sales.readDouble();
            double sueldo = sal + (ventas * 0.1);
            double deducuccion = sueldo * 0.035;
            double total = sueldo - deducuccion;
            RandomAccessFile recibos = this.billsFilefor(code);
            recibos.seek(recibos.length());
            recibos.writeLong(Calendar.getInstance().getTimeInMillis());
            recibos.writeDouble(sueldo);
            recibos.writeDouble(deducuccion);
            recibos.write(year);
            recibos.write(month);
            sales.writeBoolean(true);
            System.out.println("Nombre: "+nombre+"\n"+
                                "Sueldo: "+sueldo+"\n"+
                                "Deduccion: "+deducuccion+"\n"+
                                "Total: "+total+"\n");
        } else {
            System.out.println("No se pudo pagar");
        }
    }
    
    private RandomAccessFile billsFilefor(int code) throws IOException {
        String dirPadre = employeeFolder(code);
        String path = dirPadre + "/recibos.emp";
        return new RandomAccessFile(path, "rw");
    }
    
    public boolean isEmployeePayed(int code) throws IOException {
        RandomAccessFile sales = salesFileFor(code);
        int pos = Calendar.getInstance().get(Calendar.MONTH) * 9;
        sales.seek(pos);
        sales.skipBytes(8);
        return sales.readBoolean();
    }
}
