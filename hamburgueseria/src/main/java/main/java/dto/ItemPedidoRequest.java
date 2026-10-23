package main.java.dto;

public class ItemPedidoRequest {

    private String tipo;
    private String id;
    private int cantidad;

    public ItemPedidoRequest() {
    }

    public ItemPedidoRequest(String tipo, String id, int cantidad) {
        this.tipo = tipo;
        this.id = id;
        this.cantidad = cantidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}