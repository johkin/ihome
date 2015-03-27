package se.acrend.ihome.ruleengine

import javafx.scene.shape.Circle

/**
 *
 */
abstract class RuleBaseClass extends Script {

    def rule(Closure ruleDef) {

        RuleHolder ruleHolder = new RuleHolder()

        def rulespece = new RuleSpec()
        rulespece.holder = ruleHolder

        def code = ruleDef.rehydrate(rulespece, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        binding.setVariable("rule", ruleHolder)
    }


    class RuleSpec {
        RuleHolder holder

        def name(String name){
            holder.name = name
        }

        def when(Closure whenDef) {

        }

        def then(Closure cl) {
            holder.ruleLogic = cl
        }
    }

}
