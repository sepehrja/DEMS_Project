package FrontEnd.ServerObjectInterfaceApp;

/**
 * ServerObjectInterfaceApp/ServerObjectInterfaceHolder.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.2"
 * from C:/Users/SepJaProROG/StudioProjects/DEMS_Project/src/FrontEnd/ServerObjectInterface.idl
 * Wednesday, April 8, 2020 7:11:05 PM EDT
 */

public final class ServerObjectInterfaceHolder implements org.omg.CORBA.portable.Streamable {
    public FrontEnd.ServerObjectInterfaceApp.ServerObjectInterface value = null;

    public ServerObjectInterfaceHolder() {
    }

    public ServerObjectInterfaceHolder(FrontEnd.ServerObjectInterfaceApp.ServerObjectInterface initialValue) {
        value = initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = FrontEnd.ServerObjectInterfaceApp.ServerObjectInterfaceHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o) {
        FrontEnd.ServerObjectInterfaceApp.ServerObjectInterfaceHelper.write(o, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return FrontEnd.ServerObjectInterfaceApp.ServerObjectInterfaceHelper.type();
    }

}
