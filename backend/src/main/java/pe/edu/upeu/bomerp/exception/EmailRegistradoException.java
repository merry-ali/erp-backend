package pe.edu.upeu.bomerp.exception;
public class EmailRegistradoException extends RuntimeException {
    public EmailRegistradoException() { super("El correo ya está registrado"); }
}
