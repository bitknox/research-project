package dk.itu.raven.visualizer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.util.Pair;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.List;

import java.util.Random;

import javax.imageio.ImageIO;

public class Visualizer {
	int width, height;
	Random r = new Random();

	public Visualizer(int width, int height) {
		this.width = width;
		this.height = height;
		this.r = new Random();
	}

	public BufferedImage drawRaster(List<Pair<Geometry, Collection<PixelRange>>> results, VisualizerOptions options) {
		BufferedImage rasterImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D rasterGraphics = rasterImage.createGraphics();
		rasterGraphics.setColor(Color.white);
		rasterGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background
		for (Pair<Geometry, Collection<PixelRange>> pair : results) {
			if (options.useRandomColor) {
				rasterGraphics.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
			} else {
				rasterGraphics.setColor(options.color);
			}
			for (PixelRange range : pair.second) {
				// System.out.println(range.x2 - range.x1);
				rasterGraphics.drawLine(range.x1, range.row, range.x2, range.row);
			}
		}
		if (options.useOutput) {
			writeImage(rasterImage, options.outputPath, options.outputFormat);
		}
		return rasterImage;
	}

	public BufferedImage drawShapefile(Iterable<Geometry> features, TFWFormat transform, VisualizerOptions options) {
		BufferedImage vectorImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D vectorGraphics = vectorImage.createGraphics();
		vectorGraphics.setColor(Color.white);
		vectorGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background

		for (Geometry feature : features) {
			if (options.useRandomColor) {
				vectorGraphics.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
			} else {
				vectorGraphics.setColor(options.color);
			}
			Polygon poly = (Polygon) feature;
			Point old = poly.getFirst();
			for (Point next : poly) {
				vectorGraphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
				old = next;
			}
		}
		if (options.useOutput)

		{
			writeImage(vectorImage, options.outputPath, options.outputFormat);
		}
		return vectorImage;
	}

	public BufferedImage drawShapefile(Iterable<Geometry> features, TFWFormat transform) {
		return drawShapefile(features, transform, new VisualizerOptions());
	}

	private void writeImage(BufferedImage image, String outputPath, String outputFormat) {
		try {
			ImageIO.write(image, outputFormat, new File(outputPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
