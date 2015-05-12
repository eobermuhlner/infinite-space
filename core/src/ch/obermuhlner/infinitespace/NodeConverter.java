package ch.obermuhlner.infinitespace;


public interface NodeConverter<T> {

	public void convert (T node, RenderState renderState, boolean calculatePosition);
}
