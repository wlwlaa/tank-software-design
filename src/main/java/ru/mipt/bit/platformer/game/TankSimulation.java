package ru.mipt.bit.platformer.game;

import ru.mipt.bit.platformer.control.Command;
import ru.mipt.bit.platformer.control.KeyboardMovementStrategy;
import ru.mipt.bit.platformer.control.RandomMovementStrategy;
import ru.mipt.bit.platformer.control.TankController;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.input.InputAction;
import ru.mipt.bit.platformer.input.KeyState;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.level.RandomTankGenerator;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.TankWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Assembles and runs the game rules without a graphics context. */
public final class TankSimulation implements InputAction, Updatable {

    private final TankWorld world;
    private final Tank playerTank;
    private final List<Tank> aiTanks;
    private final List<TankController> controllers = new ArrayList<>();

    public TankSimulation(LevelDefinition level, int aiTankCount, float movementDuration,
                          KeyState keyState, List<DirectionKeyBinding> bindings, Random random) {
        this(level, aiTankCount, movementDuration, keyState, bindings, null, random);
    }

    public TankSimulation(LevelDefinition level, int aiTankCount, float movementDuration,
                          KeyState keyState, List<DirectionKeyBinding> bindings, Integer shootKey, Random random) {
        world = new TankWorld(new Field(level.getWidth(), level.getHeight(), level.getObstacles()));
        playerTank = world.addTank(level.getPlayerStart(), movementDuration);
        aiTanks = new RandomTankGenerator(random).generate(world, aiTankCount, movementDuration);

        controllers.add(new TankController(playerTank, new KeyboardMovementStrategy(keyState, bindings, shootKey)));
        for (Tank tank : aiTanks) {
            controllers.add(new TankController(tank, new RandomMovementStrategy(random)));
        }
    }

    @Override
    public void handle() {
        List<Command> commands = new ArrayList<>();
        for (TankController controller : controllers) {
            controller.nextCommand().ifPresent(commands::add);
        }
        for (Command command : commands) {
            command.execute();
        }
    }

    @Override
    public void update(float deltaTime) {
        world.update(deltaTime);
    }

    public TankWorld getWorld() {
        return world;
    }

    public Tank getPlayerTank() {
        return playerTank;
    }

    public List<Tank> getAiTanks() {
        return aiTanks;
    }
}
