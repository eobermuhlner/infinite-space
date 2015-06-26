package ch.obermuhlner.infinitespace;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.infinitespace.game.Player;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.ui.AbstractGameScreen;
import ch.obermuhlner.infinitespace.ui.NodeMenuWindow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShipUserInterface {
	private static final int CROSSHAIR_SIZE = 50;

	private static final boolean ELITE_STYLE = true;
	
	private static final String FONT = "font";
	private static final String TOUCH_KNOB = "touchKnob";
	private static final String TOUCH_BACKGROUND = "touchBackground";
	private static final String BUTTON_UP = TOUCH_KNOB;

	private static final float ORBIT_SELECTABLE_FACTOR = 10;

	private Player player;
	
	private Camera camera;

	/**
	 * Stage with corrected Viewport for text-based UI.
	 */
	private Stage stage;
	/**
	 * Stage with uncorrected Viewport for pixel accurate UI.
	 */
	private Stage stageDirect;
	private Label debugInfo;

	private Image imageCenter;
	
	private Touchpad touchpadLeft;
	private Touchpad touchpadLeftSecondary;
	private Touchpad touchpadRight;
	private Touchpad touchpadRightSecondary;

	private PlayerController playerController;

	private InfiniteSpaceGame game;
	private Skin uiSkin;
	private Skin skin;
	private LabelStyle labelStyle;
	private TouchpadStyle touchpadStyle;
	
	private AbstractGameScreen screen;

	private Environment environment;
	private Map<ModelInstance, Button> modelInstanceToButtons = new HashMap<ModelInstance, Button>();
	
	public int starSystemIndex;

	final Array<Node> massiveNodes = new Array<Node>();

	public ShipUserInterface (InfiniteSpaceGame game, Skin uiSkin, AbstractGameScreen screen, final Player player, Camera camera) {
		this.game = game;
		this.uiSkin = uiSkin;
		this.screen = screen;
		this.player = player;
		this.camera = camera;

		int screenWidth = Gdx.graphics.getWidth();
		int screenHeight = Gdx.graphics.getHeight();
		
		float thumbSize = Math.min(screenWidth, screenHeight) / 20;

		// TODO cleanup skin and use uiSking from ctor
		skin = new Skin();
		skin.add(TOUCH_BACKGROUND, new Texture("data/ui/textures/touchBackground.png"));
		skin.add(TOUCH_KNOB, new Texture("data/ui/textures/touchKnob.png"));
		skin.add(BUTTON_UP, new Texture("data/ui/textures/buttonUp.png"));
		skin.add(FONT, new BitmapFont());
		
		ScreenViewport viewport = new ScreenViewport();
		stage = new Stage(viewport);
		viewport.setUnitsPerPixel(Config.getUnitsPerPixel());

		ScreenViewport viewportDirect = new ScreenViewport();
		stageDirect = new Stage(viewportDirect);

		labelStyle = new Label.LabelStyle(skin.getFont(FONT), Color.WHITE);

		imageCenter = new Image(uiSkin, "crosshair-mini-white");
		imageCenter.setPosition(Math.round(stageDirect.getWidth() / 2), Math.round(stageDirect.getHeight() / 2));
		stageDirect.addActor(imageCenter);
		
		touchpadStyle = new TouchpadStyle();
		touchpadStyle.background = skin.getDrawable(TOUCH_BACKGROUND);
		touchpadStyle.knob = skin.getDrawable(TOUCH_KNOB);

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
		environment.add(new DirectionalLight().set(Color.WHITE, -1, 0, 0));
		
		debugInfo = new Label("", labelStyle);
		debugInfo.setBounds(0, skin.getFont(FONT).getLineHeight(), 0, 0);

		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		
		if (Config.useScreenControls) {
			float touchpadDeadZone = thumbSize / 8;
			float touchpadSize = thumbSize * 4;
			float touchpadMargin = thumbSize / 4;

			if (Config.useTouchpadControls) {
				touchpadLeft = new Touchpad(touchpadDeadZone, touchpadStyle);
				touchpadLeft.setBounds(touchpadMargin, touchpadMargin, touchpadSize, touchpadSize);
				touchpadLeftSecondary = new Touchpad(touchpadDeadZone, touchpadStyle);
				touchpadLeftSecondary.setBounds(touchpadMargin, touchpadSize+touchpadMargin, touchpadSize, touchpadSize);
				touchpadRight= new Touchpad(touchpadDeadZone, touchpadStyle);
				touchpadRight.setBounds(screenWidth-touchpadSize-touchpadMargin, touchpadMargin, touchpadSize, touchpadSize);
				touchpadRightSecondary= new Touchpad(touchpadDeadZone, touchpadStyle);
				touchpadRightSecondary.setBounds(screenWidth-touchpadSize-touchpadMargin, touchpadMargin+touchpadSize, touchpadSize, touchpadSize);
			}
			
			float buttonSize = thumbSize * 2;
			float buttonX = touchpadMargin;
			float buttonY = screenHeight-buttonSize;
			buttonX = addButton("<", buttonX, buttonY, buttonSize, touchpadMargin, new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					starSystemIndex--;
				}
			});
			buttonX = addButton(">", buttonX, buttonY, buttonSize, touchpadMargin, new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					starSystemIndex++;
				}
			});
			
			stage.addActor(debugInfo);
			if (Config.useTouchpadControls) {
				stageDirect.addActor(touchpadLeft);
				stageDirect.addActor(touchpadLeftSecondary);
				stageDirect.addActor(touchpadRight);
				stageDirect.addActor(touchpadRightSecondary);
			}
		
			inputMultiplexer.addProcessor(stage);
			inputMultiplexer.addProcessor(stageDirect);
		}
		if (Config.useKeyControls) {
			playerController = new PlayerController(player);
			inputMultiplexer.addProcessor(playerController);
		}

		inputMultiplexer.addProcessor(new InstanceSelector());
		Gdx.input.setInputProcessor(inputMultiplexer);
	}
	
	private class InstanceSelector extends InputAdapter {
		@Override
		public boolean touchDown (int screenX, int screenY, int pointer, int button) {
			Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			Vector3 center = new Vector3(0, 0, 0);
			float radius = 5;
			Vector3 intersection = new Vector3();
			boolean hit = Intersector.intersectRaySphere(ray, center, radius, intersection);
			if (hit) {
				debugInfo.setText("HIT " + intersection);
			}
			return hit;
		}
	}

	private float addButton(String text, float x, float y, float size, float margin, ClickListener clickListener) {
		TextButton buttonPrev = new TextButton(text, uiSkin, "ship");
		buttonPrev.setBounds(x, y, size, size);
		buttonPrev.addListener(clickListener);
		stageDirect.addActor(buttonPrev);
		return x + margin + size;
	}

	public void setUniverse (Iterable<Node> universe) {
		massiveNodes.clear();
		for (Node node : universe) {
			if (node instanceof OrbitingNode) {
				massiveNodes.add(node);
			}
		}
	}

	public void setZoomObject(Map<Node, Array<ModelInstance>> nodeToInstances) {
		for (Button button : modelInstanceToButtons.values()) {
			if (button != null) {
				button.remove();
			}
		}
		modelInstanceToButtons.clear();
		
		for(Array<ModelInstance> nodeInstances : nodeToInstances.values()) {
			for (int i = 0; i < nodeInstances.size; i++) {
				ModelInstance instance = nodeInstances.get(i);
				if (instance.userData instanceof UserData) {
					UserData userData = (UserData)instance.userData;
					if (userData.node != null) {
						final Node node = userData.node;
						final Button button = new Button(uiSkin, "crosshair");
						button.setSize(CROSSHAIR_SIZE, CROSSHAIR_SIZE);
						button.setUserObject(userData);
						button.addListener(new ChangeListener() {
							@Override
							public void changed (ChangeEvent event, Actor actor) {
								showMenu(button, node);							
							}
						});
						stageDirect.addActor(button);
						modelInstanceToButtons.put(instance, button);
					}
				}
			}
		}
	}
	
	private NodeMenuWindow nodeMenu;
	protected void showMenu (Button button, Node node) {
		if (nodeMenu != null) {
			nodeMenu.remove();
			nodeMenu = null;
		}

		if (!button.isChecked()) {
			// button has been unselected, don't need to show menu for it
			return;
		}
		
		for (Button cursorButton : modelInstanceToButtons.values()) {
			if (cursorButton != button) {
				cursorButton.setChecked(false);
			}
		}
		
		nodeMenu = new NodeMenuWindow(game, screen, node, uiSkin);
		nodeMenu.pack();
		nodeMenu.setPosition(0, Gdx.graphics.getHeight(), Align.top | Align.left);
		nodeMenu.top().right();
		nodeMenu.addAction(Effects.pullIn());
		
		stage.addActor(nodeMenu);
	}

	public void update() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		player.calculateHyperVelocity(massiveNodes);
		
		if (Config.useTouchpadControls) {
			inputFromTouchpad(deltaTime);
		}
		if (playerController != null) {
			playerController.update(deltaTime);
		}

		player.update(deltaTime);

		for (Map.Entry<ModelInstance, Button> entry : modelInstanceToButtons.entrySet()) {
			ModelInstance instance = entry.getKey();
			Button button = entry.getValue();

			Vector3 pos = new Vector3();
			instance.transform.getTranslation(pos);
			float distance = pos.dst(camera.position);
			if (camera.frustum.pointInFrustum(pos)) {
				Vector3 screenPos = camera.project(pos);
				button.setPosition(screenPos.x-button.getWidth()/2, screenPos.y-button.getHeight()/2);
				Node node = ((UserData) instance.userData).node;
				if (node instanceof Star || distance < getOrbit(node) * ORBIT_SELECTABLE_FACTOR) {
					button.setVisible(true);
				} else {
					button.setVisible(false);
				}
			} else {
				button.setVisible(false);
			}
		}
	}
	
	private float getOrbit(Node node) {
		if (node instanceof OrbitingNode) {
			return NodeToRenderConverter.calculateOrbitRadius((OrbitingNode) node);
		}
		return 0;
	}

	public void render() {
		stageDirect.act();
		stageDirect.draw();

		stage.act();
		stage.draw();
	}
	
	private void inputFromTouchpad (float deltaTime) {
		float thrustRight = 0;
		float thrustForward = 0;
		float thrustUp = 0;

		float roll = 0;
		float yaw = 0;
		float pitch = 0;
		
		if (ELITE_STYLE) {
			if (touchpadLeft.isTouched()) {
				player.thrustForwardThrottle.start();
			} else {
				player.thrustForwardThrottle.stop();
			}
			
			thrustForward = touchpadLeft.getKnobPercentY();
			yaw += touchpadLeft.getKnobPercentX(); 

			yaw += touchpadLeftSecondary.getKnobPercentX();
			pitch += touchpadLeftSecondary.getKnobPercentY();

			roll += touchpadRight.getKnobPercentX();
			pitch += touchpadRight.getKnobPercentY();

			thrustRight += touchpadRightSecondary.getKnobPercentX();
			thrustUp += touchpadRightSecondary.getKnobPercentY();
		} else {
			thrustRight += touchpadLeft.getKnobPercentX();
			thrustForward += touchpadLeft.getKnobPercentY();

			thrustUp += touchpadLeftSecondary.getKnobPercentY();

			yaw += touchpadRight.getKnobPercentX();
			pitch += touchpadRight.getKnobPercentY();

			roll += -touchpadRightSecondary.getKnobPercentX();
		}

		player.thrustForwardThrottle.setThrottle(thrustForward);
		player.thrustForwardThrottle.update(deltaTime);
		player.thrustUp = thrustUp;
		player.thrustRight = thrustRight;
		player.pitch = pitch;
		player.roll = roll;
		player.yaw = yaw;
	}
	
	public void dispose () {
		stage.dispose();
		stageDirect.dispose();
	}

	public void setDebugInfo (String text) {
		debugInfo.setText(text);
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		stageDirect.getViewport().update(width, height, true);
	}
}
