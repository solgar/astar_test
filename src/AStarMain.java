import java.text.DecimalFormat;
import java.util.Vector;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import playground.astar.AStar;
import playground.astar.AStarDataProvider;
import playground.astar.AStarNode;

public class AStarMain extends BasicGame implements AStarDataProvider {

	static AppGameContainer app;

	static final int size = 10;

	AStar astar;
	Vector<AStarNode> path;

	int sx = -1;
	int sy = -1;
	int ex = -1;
	int ey = -1;

	static int rows;
	static int cols;

	static int[][] mapData;
	AStarNode[][] mapNodes;

	static final int width = 1280;
    static final int height = 720;
    static double val = 0.0;//.55;

	boolean placeStart = true;
	boolean showSets = true;

	public AStarMain(String title) {
		super(title);
	}

	public static void main(String[] args) {
		try
        {
            app = new AppGameContainer(new AStarMain("AStar TEST"));
            final int width = 1280;
            final int height = 720;
            cols = width / size;
            rows = height / size;
            mapData = new int[rows][cols];
            app.setDisplayMode(width, height, false);
//            app.setDisplayMode(cols * size, rows * size, false);
            app.setMouseGrabbed(false);
            app.setShowFPS(false);
            app.start();
        }
        catch (SlickException e)
        {
            e.printStackTrace();
        }
	}

	@Override
	synchronized public void render(GameContainer game, Graphics g) throws SlickException {
		g.setBackground(Color.white);
		g.clear();

		if (showSets) {
			for (AStarNode node : astar.closedSet) {
				g.setColor(Color.lightGray);
				g.fillRect(node.x*size, node.y*size, size, size);
			}

			for (AStarNode node : astar.openSet) {
				g.setColor(Color.gray);
				g.fillRect(node.x*size, node.y*size, size, size);
			}
		}

		for (int y = 0; y < mapData.length; y++ ) {
			for (int x = 0; x < mapData[y].length; x++) {
				int c = mapData[y][x];
				if (c == '#') {
					g.setColor(Color.black);
					g.fillRect(x*size, y*size, size, size);
				}
			}
		}

		if (getStartNode() != null) {
			g.setColor(Color.green);
			g.fillRect(getStartNode().x*size, getStartNode().y*size, size, size);
		}

		if (getDestinationNode() != null) {
			g.setColor(Color.red);
			g.fillRect(getDestinationNode().x*size, getDestinationNode().y*size, size, size);
		}

		if (path != null)
			for (AStarNode node : path)
				drawNode(g, node);

		g.setColor(Color.red);
		g.drawString("steps: " + (path == null ? 0 : path.size()), width - 130, 18);
		DecimalFormat threeDec = new DecimalFormat("0.00");
		g.drawString("additional cost: " + threeDec.format(val), width - 220, 36);
		g.drawString("astar.stopAfter: " + astar.stopAfter, width - 220, 54);
	}

	public void drawNode(Graphics g, AStarNode node) {
		if (!showSets && node != getDestinationNode()) {
			g.setColor(Color.lightGray);
			g.fillRect(node.x*size, node.y*size, size, size);
		}

		g.setColor(Color.blue);
		g.drawRect(node.x*size, node.y*size, size, size);
		if (node.parent != null) {
			int smallSize = 4;
			g.setColor(Color.black);
			g.fillRect(node.x*size + size/2 - smallSize/2, node.y*size + size/2 - smallSize/2, smallSize, smallSize);
			g.setColor(Color.blue);
			g.drawLine(node.x*size + size/2, node.y*size + size/2,
					node.parent.x*size + size/2, node.parent.y*size + size/2);
		}
	}

	@Override
	public void init(GameContainer game) throws SlickException {
		simulation = false;
		path = null;
		astar = new AStar(this);
		mapNodes = new AStarNode[mapData.length][];
		for (int y = 0; y < mapData.length; y++ ) {
			mapNodes[y] = new AStarNode[mapData[y].length];
			for (int x = 0; x < mapData[y].length; x++) {
				mapNodes[y][x] = new AStarNode(x, y);
				mapData[y][x] = 0;
			}
		}

		for (int i = 0; i < mapData.length; i++) {
        	mapData[i][0] = '#';
        	mapData[i][mapData[i].length - 1] = '#';
		}

		for (int i = 0; i < mapData[0].length; i++) {
			mapData[0][i] = '#';
			mapData[mapData.length - 1][i] = '#';
		}
	}

	boolean simulation = false;

	public void update(GameContainer game, int dt) throws SlickException {
		Input in = game.getInput();
		int mx = (int) (in.getMouseX() / size);
		int my = (int) (in.getMouseY() / size);

		if (simulation) {
			boolean dontBuildUpStopAfter = false;
			if (astar.pathComplete == true) {
				dontBuildUpStopAfter = true;
				astar.pathComplete = false;
			}

			if (astar.pathComplete == false) {
				for (int y = 0; y < mapData.length; y++ ) {
					for (int x = 0; x < mapData[y].length; x++) {
						mapNodes[y][x].steps = 0;
						mapNodes[y][x].parent = null;
						mapNodes[y][x].distance = 0;
						mapNodes[y][x].additionalCost = 0;
					}
				}
				neighbourNum = 0;
				currNode = null;
				path = astar.find();
				if (!dontBuildUpStopAfter)
					astar.stopAfter += 1;
//				try {
//					Thread.sleep(2);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}

		if (in.isKeyPressed(Input.KEY_UP)) val += 0.1;
		if (in.isKeyPressed(Input.KEY_DOWN)) val -= 0.1;
		if (in.isKeyPressed(Input.KEY_RIGHT)) val += 1.0;
		if (in.isKeyPressed(Input.KEY_LEFT)) val -= 1.0;
		if (in.isKeyPressed(Input.KEY_0) || in.isKeyPressed(Input.KEY_NUMPAD0)) val = 0;

		if (in.isKeyPressed(Input.KEY_SPACE))
			showSets = !showSets;

		if (in.isKeyPressed(Input.KEY_S)) {
			if (simulation) {
				simulation = false;
			}
			else {
				astar.pathComplete = false;
				simulation = true;
				astar.stopAfter = 1;
			}
		}

		if (in.isKeyPressed(Input.KEY_C)) {
			init(null);
		}

		if (in.isKeyPressed(Input.KEY_B)) {
			init(null);
			for (int y = 0; y < mapData.length; y++ ) {
				for (int x = 0; x < mapData[y].length; x++) {
					mapData[y][x] = '#';
				}
			}
		}

		if (in.isKeyPressed(Input.KEY_F)) {
			simulation = false;
			long stime = System.currentTimeMillis();
//			for (int i = 1; i < 2496; i++) {
			for (int i = 1; i < 2; i++) {
				for (int y = 0; y < mapData.length; y++ ) {
					for (int x = 0; x < mapData[y].length; x++) {
						mapNodes[y][x].steps = 0;
						mapNodes[y][x].parent = null;
						mapNodes[y][x].distance = 0;
						mapNodes[y][x].additionalCost = 0;
					}
				}
				neighbourNum = 0;
				currNode = null;
				astar.stopAfter = -1;
				path = astar.find();
			}
			System.out.println("astar took: " + (System.currentTimeMillis() - stime) + "  path steps: " + path.size());
		}

		if (in.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON))
		{
			putPiece('#', mx, my);
			putPiece('#', mx + 1, my);
			putPiece('#', mx, my + 1);
			putPiece('#', mx + 1, my + 1);
		}
		else if (in.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON))
		{
			putPiece(0, mx, my);
			putPiece(0, mx + 1, my);
			putPiece(0, mx, my + 1);
			putPiece(0, mx + 1, my + 1);
		}

		if (in.isMousePressed(Input.MOUSE_MIDDLE_BUTTON)) {
			if (in.isKeyDown(Input.KEY_LCONTROL)) {
				if (sx != -1)
					putPiece(0, sx, sy);
				putPiece('s', mx, my);
				sx = mx;
				sy = my;
			}
			else {
				if (ex != -1)
					putPiece(0, ex, ey);
				putPiece('d', mx, my);
				ex = mx;
				ey = my;
			}
			placeStart = !placeStart;
		}

		if (in.isKeyDown(Input.KEY_ADD)) {
			if (astar.pathComplete == false) {
				astar.stopAfter += 1;
				for (int y = 0; y < mapData.length; y++ ) {
					for (int x = 0; x < mapData[y].length; x++) {
						mapNodes[y][x].steps = 0;
						mapNodes[y][x].parent = null;
						mapNodes[y][x].distance = 0;
					}
				}
				neighbourNum = 0;
				currNode = null;
				path = astar.find();
			}
			else {
				System.out.println("Path complete! Steps: " + path.size());
			}
		} else if (in.isKeyDown(Input.KEY_SUBTRACT)) {
			astar.stopAfter -= 1;
			if (astar.stopAfter < 0)
				astar.stopAfter = 0;
			for (int y = 0; y < mapData.length; y++ ) {
				for (int x = 0; x < mapData[y].length; x++) {
					mapNodes[y][x].steps = 0;
					mapNodes[y][x].parent = null;
					mapNodes[y][x].distance = 0;
				}
			}
			neighbourNum = 0;
			currNode = null;
			path = astar.find();
		}

	}

	int neighbourNum = 0;
	AStarNode currNode = null;
	public AStarNode nextNeighbour(AStarNode node) {
		if (currNode == null) {
			neighbourNum = 0;
			currNode = node;
		}
		else if (neighbourNum == 8) {
			currNode = null;
			neighbourNum = 0;
			return null;
		}

		int x = -1;
		int y = -1;
		AStarNode neighbour = null;
		double additionalCost = 0;

		if      (neighbourNum == 0) { y = node.y - 1; x = node.x - 1; additionalCost = val; }
		else if (neighbourNum == 1) { y = node.y - 1; x = node.x; }
		else if (neighbourNum == 2) { y = node.y - 1; x = node.x + 1; additionalCost = val; }
		else if (neighbourNum == 3) { y = node.y;     x = node.x - 1; }
		else if (neighbourNum == 4) { y = node.y;     x = node.x + 1; }
		else if (neighbourNum == 5) { y = node.y + 1; x = node.x - 1; additionalCost = val; }
		else if (neighbourNum == 6) { y = node.y + 1; x = node.x; }
		else if (neighbourNum == 7) { y = node.y + 1; x = node.x + 1; additionalCost = val; }

		neighbourNum += 1;

		if (x < 0 || y < 0 || y >= mapNodes.length || x >= mapNodes[0].length || mapData[y][x] == '#')
			neighbour = nextNeighbour(node);
		else
			neighbour = mapNodes[y][x];

		if (neighbour != null)
			neighbour.additionalCost += additionalCost;

		return neighbour;
	}

	public AStarNode getStartNode() {
		if (sx == -1)
			return null;
		return mapNodes[sy][sx];
	}
	public AStarNode getDestinationNode() {
		if (ex == -1)
			return null;
		return mapNodes[ey][ex];
	}

	public void putPiece(int piece, int x, int y) {
		if (x < 0) x = 0;
		else if (x >= mapData[0].length) x = mapData[0].length - 1;
		if (y < 0) y = 0;
		else if (y >= mapData.length) y = mapData.length - 1;
		mapData[y][x] = piece;
	}
}
