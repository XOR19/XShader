package xshader.gui.node;


public class GuiLabel extends Component {
	
	private String text;
	
	private int align;
	
	private float px;
	
	private float py;
	
	private float width;
	
	private float height;
	
	public GuiLabel(Component parent, String text, int align) {
		super(parent);
		this.text = text;
		this.align = align;
		this.height = fontRenderer.FONT_HEIGHT;
	}

	@Override
	public void setX(float x) {
		this.px = x;
	}
	
	@Override
	public void setY(float y) {
		this.py = y;
	}
	
	@Override
	public float getX() {
		return this.px;
	}
	
	@Override
	public float getY() {
		return this.py;
	}
	
	@Override
	public float getHeight() {
		return this.height;
	}
	
	@Override
	public void setHeight(float height) {
		this.height = height;
	}
	
	@Override
	public void setWidth(float width) {
		this.width = width;
	}
	
	@Override
	public void render(float x, float y) {
		drawString(this.text, x+this.px, y+this.py, this.width, this.height, this.align);
	}
	
	@Override
	public Component getComponentAt(float x, float y) {
		return null;
	}
	
}
