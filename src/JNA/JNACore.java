package JNA;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JNACore {

    /*
     Access modes
     */
    public static final int PROCESS_QUERY_INFORMATION = 0x0400;
    public static final int PROCESS_VM_READ = 0x0010;
    public static final int PROCESS_VM_WRITE = 0x0020;
    public static final int PROCESS_VM_OPERATION = 0x0008;
    public static final int PROCESS_ALL_ACCESS = 0x001F0FFF;

    /*
    Zezenia client objects
    */
    private static int zezeniaPID;
    private static Pointer zezeniaProcessHandle;

    /*
     Fills the processList array with process id's.
     */
    public static void getProcesses() {
        int[] processList = new int[1024];
        int[] dummyList = new int[1024];
        Psapi.INSTANCE.EnumProcesses(processList, 1024, dummyList);

        int pid;
        int i = 0;
        while (i < processList.length) {
            pid = processList[i];
            if (pid != 0) {
                Pointer ph = Kernel32.INSTANCE.OpenProcess(PROCESS_ALL_ACCESS, false, pid);
                if (ph != null) {
                    byte[] filename = new byte[512];
                    Psapi.INSTANCE.GetModuleBaseNameW(ph, new Pointer(0), filename, 512);
                    String test = new String(filename);
                    test = test.trim();
                    if (test.contains("Z e z e n i a . e x e")) {
                        zezeniaPID = pid;
                        zezeniaProcessHandle = ph;
                        System.out.println(pid + "\n"
                                + test+"\n"+ph.toString()+"\n");
                        break;
                    }
                    Kernel32.INSTANCE.CloseHandle(ph);
                }
            }
            i++;
        }
    }

    /*
     Returns a pointer the a process given by a pid.
     */
    private static Pointer returnProcess(int pid) {
        Pointer process = Kernel32.INSTANCE.OpenProcess(PROCESS_ALL_ACCESS, false, pid);
        return process;
    }

    /*
     * Reads the specified number of bytes in the specified memory location
     * of the specified process.
     */
    private static Memory readMemory(Pointer process, long address, int bytesToRead) {
        IntByReference read = new IntByReference(0);
        Memory output = new Memory(bytesToRead);

        Kernel32.INSTANCE.ReadProcessMemory(process, address, output, bytesToRead, read);
        return output;

    }

    /*
     * Writes the specified number of bytes at the specified memory location
     * of the specified process.
     */
    private void writeMemory(Pointer process, long address, byte[] data) {
        int size = data.length;
        Memory toWrite = new Memory(size);

        for (int i = 0; i < size; i++) {
            toWrite.setByte(i, data[i]);
        }

        boolean b = Kernel32.INSTANCE.WriteProcessMemory(process, address, toWrite, size, null);
    }

}
