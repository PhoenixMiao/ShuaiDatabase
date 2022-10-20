import java.util.concurrent.CopyOnWriteArrayList;

public class ShuaiList extends ShuaiObject{

    private CopyOnWriteArrayList<ShuaiObject> list;

    public ShuaiList() {
        this.list = new CopyOnWriteArrayList<>();
    }

    public CopyOnWriteArrayList<ShuaiObject> getList() {
        return list;
    }
}
