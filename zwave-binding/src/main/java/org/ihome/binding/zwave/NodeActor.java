package org.ihome.binding.zwave;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.ihome.springactor.Actor;
import org.zwave4j.ValueId;

/**
 *
 */
@Actor("NodeActor")
public abstract class NodeActor extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    short nodeId;

    static class ValueAdded {
        private ValueId valueId;

        public ValueAdded(ValueId valueId) {
            this.valueId = valueId;
        }

        public ValueId getValueId() {
            return valueId;
        }
    }

    static class ValueRemoved {
        private ValueId valueId;

        public ValueRemoved(ValueId valueId) {
            this.valueId = valueId;
        }

        public ValueId getValueId() {
            return valueId;
        }
    }
    static class ValueChanged {
        private ValueId valueId;

        public ValueChanged(ValueId valueId) {
            this.valueId = valueId;
        }

        public ValueId getValueId() {
            return valueId;
        }
    }
    static class ValueRefreshed {
        private ValueId valueId;

        public ValueRefreshed(ValueId valueId) {
            this.valueId = valueId;
        }

        public ValueId getValueId() {
            return valueId;
        }
    }
}
