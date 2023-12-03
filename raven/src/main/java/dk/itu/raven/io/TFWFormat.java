package dk.itu.raven.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.PixelCoordinate;

public class TFWFormat {

	double pixelLengthX, rotationY, rotationX, pixelLengthYNegative, pixelLengthY, topLeftX, topLeftY,
			inveresePixelLengthX, inveresePixelLengthY;

	public TFWFormat(double pixelLengthX, double rotationY, double rotationX, double pixelLengthYNegative,
			double topLeftX, double topLeftY) {
		this.pixelLengthX = pixelLengthX;
		this.rotationY = rotationY;
		this.rotationX = rotationX;
		this.pixelLengthYNegative = pixelLengthYNegative;
		this.pixelLengthY = pixelLengthYNegative;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.inveresePixelLengthX = 1.0 / pixelLengthX;
		this.inveresePixelLengthY = 1.0 / pixelLengthY;
	}

	static TFWFormat read(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		double pixelLengthX = Double.parseDouble(br.readLine());
		double rotationY = Double.parseDouble(br.readLine());
		double rotationX = Double.parseDouble(br.readLine());
		double pixelLengthYNegative = Double.parseDouble(br.readLine());
		double topLeftX = Double.parseDouble(br.readLine());
		double topLeftY = Double.parseDouble(br.readLine());
		br.close();
		return new TFWFormat(pixelLengthX, rotationY, rotationX, pixelLengthYNegative, topLeftX, topLeftY);
	}

	public Point transformFromPixelToCoordinate(double x, double y) {
		double xCoordinate = topLeftX + (pixelLengthX * x);
		double yCoordinate = topLeftY + (pixelLengthY * y);
		return Geometries.point(xCoordinate, yCoordinate);
	}

	public Point transFromCoordinateToPixel(double lat, double lon) {
		double xPixel = (lat - topLeftX) * inveresePixelLengthX;
		double yPixel = (lon - topLeftY) * inveresePixelLengthY;
		return Geometries.point(xPixel, yPixel);
	}
}
