import java.awt.EventQueue; 

public class Main {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) { 
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileDragAndDrop window = new FileDragAndDrop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});  
	}
}
