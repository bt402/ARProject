package greenwich.pointr;

public class Model{
    String name;
    int value;

    Model(String name, int value){
        this.name = name;
        this.value = value;
    }
    public String getName(){
        return this.name;
    }
    public int getValue(){
        return this.value;
    }
    public void setValue(int value) { this.value = value; }

}
