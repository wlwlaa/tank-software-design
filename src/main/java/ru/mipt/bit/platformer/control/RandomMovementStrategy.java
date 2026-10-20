package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class RandomMovementStrategy implements MovementStrategy {

    private static final Direction[] DIRECTIONS = Direction.values();

    private final Random random;

    public RandomMovementStrategy(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public Optional<Command> nextCommand(MovementTarget target) {
        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        return Optional.of(new MoveCommand(target, direction));
    }
}
