
public class SkipListNode<T>
{	
	// skip list nodes in each direction
	private SkipListNode<T> up;
	private SkipListNode<T> down;
	private SkipListNode<T> left;
	private SkipListNode<T> right;
	private T value;
	
	public SkipListNode(T value)
	{
		this.SetValue(value);
	}
	
	// Node Get and Set functions
	// Up
	public SkipListNode<T> GetUp()
	{
		return up;
	}
	public void SetUp(SkipListNode<T> up)
	{
		this.up = up;
	}
	
	// Down
	public SkipListNode<T> GetDown()
	{
		return down;
	}
	public void SetDown(SkipListNode<T> down)
	{
		this.down = down;
	}
	
	// Left
	public SkipListNode<T> GetLeft()
	{
		return left;
	}
	public void SetLeft(SkipListNode<T> left)
	{
		this.left = left;
	}
	
	// Right
	public SkipListNode<T> GetRight()
	{
		return right;
	}
	public void SetRight(SkipListNode<T> right)
	{
		this.right = right;
	}
	
	// Value Get and Set Functions
	public T GetValue() {
		return value;
	}
	public void SetValue(T value) {
		this.value = value;
	}
}
