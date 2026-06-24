package main.java.events;

public interface EventType {
    /**
     * Die verschiedenen EventTypes, die eine Event haben kann
     */
     enum Type{
        EVENT_CLOSE ,
        EVENT_OPEN ,
        EVENT_READ ,
        EVENT_WRITE ,
        EVENT_EXIT ,
        EVENT_FORK ,
        EVENT_EXECUTE ,
        EVENT_CONNECT ,
        EVENT_ACCEPT ,
        EVENT_CREATE_OBJECT ,
        EVENT_LSEEK ,
        EVENT_MMAP ,
        EVENT_MODIFY_PROCESS ,
       EVENT_MODIFY_FILE_ATTRIBUTE,
        EVENT_CHANGE_PRINCIPAL ,
        EVENT_RENAME,
        EVENT_SENDTO,
        EVENT_RESCVMSG,
        EVENT_RECVFROM,
        EVENT_UNLINK,
        EVENT_SENDMSG,
       EVENT_SINAL,
       EVENT_TRUNCATE,
       EVENT_BIND,
       EVENT_LINK
    }
}
