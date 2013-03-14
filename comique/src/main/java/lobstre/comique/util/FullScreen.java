package lobstre.comique.util;

import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.*;

@SuppressWarnings("serial")
public class FullScreen extends JFrame {
    public FullScreen() {
        add(new JButton("OK"));
        enableOSXFullscreen(this);
        setVisible(true);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void enableOSXFullscreen(Window window) {
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void requestToggleFullScreen(Window window)
    {
        try {
            Class appClass = Class.forName("com.apple.eawt.Application");
            Class params[] = new Class[]{};

            Method getApplication = appClass.getMethod("getApplication", params);
            Object application = getApplication.invoke(appClass);
            Method requestToggleFulLScreen = application.getClass().getMethod("requestToggleFullScreen", Window.class);

            requestToggleFulLScreen.invoke(application, window);
        } catch (Exception e) {
            System.out.println("An exception occurred while trying to toggle full screen mode");
        }
    }

    public static void main(String[] args) {
        FullScreen fs = new FullScreen();
        FullScreen.requestToggleFullScreen(fs);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {}

        FullScreen.requestToggleFullScreen(fs);
    }
}