/*Resource objects stores the resource type, 
 * the total number of this resource we have and 
 after allocated to tasks, the remaining number of this resource*/

public class Resource {
	private int resource_type;
	
	private int total;
	
	private int remaining;
	
	public Resource(int resource_type){
		this.resource_type=resource_type;
	}

	public int getResource_type() {
		return resource_type;
	}

	public void setResource_type(int resource_type) {
		this.resource_type = resource_type;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
		this.remaining=total;
	}

	public int getRemaining() {
		return remaining;
	}

	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}
	
	
}
