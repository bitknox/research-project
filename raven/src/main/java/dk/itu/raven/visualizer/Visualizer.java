package dk.itu.raven.visualizer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.Node;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.Square;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.matrix.Matrix;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;

import java.awt.BasicStroke;
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

	public BufferedImage drawRaster(List<Pair<Geometry, Collection<PixelRange>>> results, Iterable<Polygon> features,
			VisualizerOptions options) {
		BufferedImage rasterImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_INDEXED);
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
				// Logger.log(range.x2 - range.x1);
				rasterGraphics.drawLine(range.x1, range.row, range.x2, range.row);
			}
		}
		rasterGraphics.setColor(Color.RED);
		for (Polygon poly : features) {
			Point old = poly.getFirst();
			for (Point next : poly) {
				rasterGraphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
				old = next;
			}
		}
		if (options.useOutput) {
			writeImage(rasterImage, options.outputPath, options.outputFormat);
		}
		return rasterImage;
	}

	public BufferedImage drawShapefile(Iterable<Polygon> features, TFWFormat transform, VisualizerOptions options) {
		BufferedImage vectorImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D vectorGraphics = vectorImage.createGraphics();
		vectorGraphics.setColor(Color.white);
		vectorGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background

		for (Polygon poly : features) {
			if (options.useRandomColor) {
				vectorGraphics.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
			} else {
				vectorGraphics.setColor(options.color);
			}
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

	public BufferedImage drawShapefile(Iterable<Polygon> features, TFWFormat transform) {
		return drawShapefile(features, transform, new VisualizerOptions());
	}

	private void drawMbr(Node<String, Geometry> node, Graphics2D graphics) {
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(new Color(0,0,255));
		for (Node<String, Geometry> child : TreeExtensions.getChildren(node)) {
			int width = (int) (child.geometry().mbr().x2() - child.geometry().mbr().x1());
			int height = (int) (child.geometry().mbr().y2() - child.geometry().mbr().y1());
			graphics.drawRect((int) child.geometry().mbr().x1(), (int) child.geometry().mbr().y1(),
					width, height);
			if (!TreeExtensions.isLeaf(child)) {
				drawMbr(child, graphics);
			}
		}
	}

	private void drawK2Squares(K2Raster k2Raster, int k2Index, Square rasterBounding, int level, Graphics2D graphics) {
		if (level > 14)
			return;
		graphics.setColor(new Color(0, 255, 0));
		int[] children = k2Raster.getChildren(k2Index);
		int childSize = rasterBounding.getSize() / K2Raster.k;
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				Square childRasterBounding = rasterBounding.getChildSquare(childSize, i, K2Raster.k);
				graphics.drawRect(childRasterBounding.getTopX(), childRasterBounding.getTopY(), childRasterBounding.getSize(), childRasterBounding.getSize());
				drawK2Squares(k2Raster, child, childRasterBounding, level + 1, graphics);
			}
	}

	public BufferedImage drawK2SquareImage(K2Raster k2Raster) {
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, this.width, this.height); 
		drawK2Squares(k2Raster, 0, new Square(0, 0, k2Raster.getSize()), 0, graphics);

		writeImage(image, "./k2_squares.png", "png");

		return image;
	}

	public BufferedImage drawVectorRasterOverlap(Iterable<Polygon> features, Matrix m, RTree<String, Geometry> tree, K2Raster k2Raster) {
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background

		

		// for (int i = 0; i < m.getWidth(); i++) {
		// 	for (int j = 0; j < m.getHeight(); j++) {
		// 		int val = 20*m.get(i, j);
		// 		graphics.setColor(new Color(val, val, val));
		// 		graphics.drawLine(i, j, i, j);
		// 	}
		// }

		drawK2Squares(k2Raster, 0, new Square(0, 0, k2Raster.getSize()), 0, graphics);

		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(new Color(255,0,0));
		
		for (Polygon poly : features) {
			Point old = poly.getFirst();
			for (Point next : poly) {
				graphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
				old = next;
			}
		}

		drawMbr(tree.root().get(), graphics);

		writeImage(image, "./vector_raster_overlap.png", "png");

		return image;

	}
	

	private void writeImage(BufferedImage image, String outputPath, String outputFormat) {
		try {
			ImageIO.write(image, outputFormat, new File(outputPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
