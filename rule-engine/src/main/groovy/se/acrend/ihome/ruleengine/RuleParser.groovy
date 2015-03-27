package se.acrend.ihome.ruleengine

import org.codehaus.groovy.control.CompilerConfiguration


/**
 *
 */
class RuleParser {

    RuleHolder readRule(File ruleFile) {

        def config = new CompilerConfiguration()
        config.scriptBaseClass = RuleBaseClass.name


        def binding = new Binding()
        def shell = new GroovyShell(this.class.classLoader, binding, config)
        binding.setVariable('x',1)
        binding.setVariable('y',3)

        def script = shell.parse ruleFile.text
        script.run()


        def scriptMethods = RuleBaseClass.metaClass.methods

        script.metaClass.methods.findAll({
            !scriptMethods.contains(it)
        }).forEach {
            println it.name
        }


        binding.getVariable("rule")
    }


}
