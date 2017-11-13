package com.runesuite.client.updater.mapper.std.classes

import com.runesuite.mapper.IdentityMapper
import com.runesuite.mapper.annotations.DependsOn
import com.runesuite.mapper.extensions.Predicate
import com.runesuite.mapper.extensions.predicateOf
import com.runesuite.mapper.tree.Class2

@DependsOn(Client.Players_targetIndices::class)
class Players : IdentityMapper.Class() {

    override val predicate = predicateOf<Class2> { field<Client.Players_targetIndices>().klass == it }
}