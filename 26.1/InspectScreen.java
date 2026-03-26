public class InspectScreen {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("net.minecraft.client.gui.screens.Screen");
    for (var m : c.getDeclaredMethods()) {
      if (m.getName().equals("render") || m.getName().equals("renderBg") || m.getName().equals("renderContents") || m.getName().equals("renderLabels")) {
        System.out.println(m.toString());
      }
    }
  }
}
