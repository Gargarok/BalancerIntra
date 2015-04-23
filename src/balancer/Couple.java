package balancer;

public class Couple<T,V> {
  private T a;
  private V b;
  
  public Couple(T a, V b) {
    this.a = a;
    this.b = b;
  }
  
  public T getA() {
    return a;
  }
  public V getB() {
    return b;
  }
}



