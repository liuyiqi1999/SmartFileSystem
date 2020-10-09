package Id;

import java.util.HashMap;
import java.util.Map;

public class IdImplFactory{
    private static IdImplFactory idImplFactory = null;
    private IdImplFactory(){
    }
    public static IdImplFactory getInstance() {
        if(idImplFactory == null) {
            idImplFactory = new IdImplFactory();
        }
        return idImplFactory;
    }

    Map<Class<?>,IdImplCreator<?>> classIdImplFactoryMap = new HashMap<>();

    static class IdImplCreator<T>{
        int localId=0;
        Class<T> clazz;

        public IdImplCreator(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Id<T> getNewId(){
            return new IdImpl<>(localId++,clazz);
        }
    }

    public <T> Id<T> getNewId(Class<T> clazz){
        if (this.classIdImplFactoryMap.get(clazz)!=null) {
            return ((IdImplCreator<T>)(this.classIdImplFactoryMap.get(clazz))).getNewId();
        } else {
            IdImplCreator<T> idImplCreator = new IdImplCreator<>(clazz);
            this.classIdImplFactoryMap.put(clazz,idImplCreator);
            return idImplCreator.getNewId();
        }
    }
}

class IdImpl<T> implements Id<T> {
    Class<T> clazz;
    int id;

    IdImpl(int id,Class<T> clazz) {
        this.id=id;
        this.clazz = clazz;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        switch (this.clazz.getSimpleName()) {
            case "Block":
                return "b" + this.id;
            case "File":
                return "f" + this.id;
            case "BlockManager":
                return "bm" + this.id;
            case "FileManager":
                return "fm" + this.id;
            default:
                return "";//TODO
        }
    }
}