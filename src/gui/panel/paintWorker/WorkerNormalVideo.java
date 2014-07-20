/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.panel.paintWorker;

import controller.Controller;
import gui.panel.jPanelVideoPlayer;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.SwingWorker;
import sharedThreadResources.DataShare;

/**
 *
 * @author nick
 */
public class WorkerNormalVideo extends SwingWorker<Void, Image> implements IWorkerInterface {

    private jPanelVideoPlayer jpanl;
    private volatile boolean stop = false; //stop flag

    public WorkerNormalVideo() {
    }

    @Override
    protected void process(List<Image> chunks) {
        for (Image bufferedImage : chunks) {
//            src = bufferedImage;
            jpanl.setSrc(bufferedImage);
            jpanl.repaint();
        }
    }

// ovo radi u pozadini
    @Override
    protected Void doInBackground() throws Exception {
        if (jpanl == null) {
            throw new RuntimeException("Unesite top panel");
        }
        int frames = 0;

        long start = System.currentTimeMillis();
//            tajmer trajanje
        long end = start + 40000;
        long last = start;

        DataShare dataStore = Controller.getInstance().getDataStore();
//

        ArrayBlockingQueue arrayList = dataStore.getListImage();
        BufferedImage bImage1 = null;
        BufferedImage bImage2 = null;
        boolean flag = false;
        int count = 0;
        while (stop == false) {
            if (flag == false) {

                bImage1 = (BufferedImage) arrayList.take();
                flag = true;
                count++;
            } else {
                bImage2 = (BufferedImage) arrayList.take();
                flag = false;
                count++;
            }

            if (count == 2) {
                count = 0;
                Controller.getInstance().compare(bImage1, bImage2);
                BufferedImage bi = Controller.getInstance().getImgNormalRectangle();
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(bi, 0, 0, null);
                g2.dispose();
                publish(bi);
                last = System.currentTimeMillis();
                frames++;
                if (frames % 30 == 0) {
                    String fps = String.valueOf((double) frames / (last - start) * 1000);
                    System.out.println("Frames = " + frames + ", fps = " + fps);
                }
            }
        }
        System.out.println("Stopirano!");
        return null;
    }

    @Override
    public void setTopPanel(jPanelVideoPlayer aThis) {
        this.jpanl = aThis;
    }

    //stop metoda
    @Override
    public void stopExecuting() {
        stop = true;
    }
}
