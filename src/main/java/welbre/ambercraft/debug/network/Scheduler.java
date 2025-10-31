package welbre.ambercraft.debug.network;

import java.util.LinkedList;
import java.util.function.Consumer;

public class Scheduler {
    private final LinkedList<Task> tasks = new LinkedList<>();

    /// @param  delay in seconds
    public Task schedule(float delay, Consumer<Task> onExpire)
    {
        var task = new Task(delay, onExpire);
        tasks.add(task);
        return task;
    }

    /// @param delay_ticks in ticks
    public Task schedule(int delay_ticks, Consumer<Task> onExpire) {
        return schedule(delay_ticks * 20f, onExpire);
    }

    /// @param deadTime in seconds
    /// @param frequency in Hz
    /// @param delay in seconds
    public EachTickTask scheduleEachTick(float deadTime, float frequency, float delay, Consumer<Task> onExpire, Consumer<Task> eachTick)
    {
        var task = new EachTickTask(deadTime, frequency, delay, onExpire, eachTick);
        tasks.add(task);
        return task;
    }

    public <T extends Task> T add(T task)
    {
        if (task != null)
            tasks.add(task);

        return task;
    }

    public void removeTask(Task task)
    {
        tasks.remove(task);
    }

    public void tick()
    {
        tick(0.05f);//default minecraft tick time
    }

    /// @param dt in seconds
    public void tick(float dt)
    {
        for (Task task : tasks)
            task.tick(dt);

        tasks.removeIf(a -> a.shouldRemove);
    }

    public void clear(Class<? extends Task> clazz)
    {
        tasks.removeIf(a -> a.getClass().equals(clazz));
    }

    public void clear()
    {
        tasks.clear();
    }


    public static class Task {
        /// in seconds
        public final float delay;
        public final Consumer<Task> task;

        protected boolean shouldRemove = false;
        /// in seconds
        public float time = 0;

        /// @param delay in seconds
        public Task(float delay, Consumer<Task> task) {
            this.delay = delay;
            this.task = task;
        }

        /// @param dt in seconds
        public void tick(float dt)
        {
            time += dt;
            if (time >= delay)
            {
                task.accept(this);
                shouldRemove = true;
            }
        }

        public boolean isDone() {
            return time >= delay;
        }

        public void markToRemove() {
            shouldRemove = true;
        }
    }

    public static class EachTickTask extends Task
    {
        /// in seconds
        private final float deadTime;
        /// in Hz
        private final float frequency;
        public final Consumer<Task> each;

        /// in seconds
        private float timer;

        /// @param deadTime in seconds
        /// @param frequency in Hz
        /// @param delay in seconds
        public EachTickTask(float deadTime, float frequency, float delay, Consumer<Task> task, Consumer<Task> each) {
            super(delay, task);
            this.deadTime = deadTime;
            this.frequency = frequency;
            this.each = each;
        }

        /// uses 0 as Dead time, and frequency
        /// @param delay in seconds
        public EachTickTask(float delay, Consumer<Task> task, Consumer<Task> each) {
            this(0f,0f, delay,task,each);
        }

        /// @param dt in seconds
        @Override
        public void tick(float dt) {
            super.tick(dt);
            if (deadTime <= time)
                if (timer >= frequency)
                {
                    each.accept(this);
                    timer -= frequency;
                }
                else
                    timer += dt;
        }
    }
}
