/*
    Copyright(c) 2021 AuroraLS3

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/
package net.playeranalytics.extension.mypet;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.DataBuilderProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.TabInfo;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.entity.MyPetType;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.plugin.MyPetPlugin;
import de.Keyle.MyPet.api.repository.PlayerManager;
import de.Keyle.MyPet.api.skill.Skills;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * DataExtension.
 *
 * @author AuroraLS3
 */
@PluginInfo(name = "MyPet", iconName = "paw", iconFamily = Family.SOLID, color = Color.GREEN)
@TabInfo(tab = "Pet skills", iconName = "book", elementOrder = {ElementOrder.VALUES, ElementOrder.TABLE})
public class MyPetExtension implements DataExtension {

    private MyPetPlugin myPet;

    public MyPetExtension() {
        myPet = (MyPetPlugin) Bukkit.getPluginManager().getPlugin("MyPet");
    }

    public MyPetExtension(boolean forTesting) { }

    @DataBuilderProvider
    public ExtensionDataBuilder playerData(UUID playerUUID) {
        MyPetPlayer player = getMyPetPlayer(playerUUID);

        boolean hasPet = player.hasMyPet();
        MyPet pet = player.getMyPet();
        Optional<MyPet> petOptional = Optional.ofNullable(pet);

        String petType = petOptional
                .flatMap(MyPet::getEntity)
                .map(MyPetBukkitEntity::getPetType)
                .map(MyPetType::name)
                .orElse(null);

        String petName = petOptional
                .map(MyPet::getPetName)
                .orElse(null);

        Table.Factory skillTable = Table.builder()
                .columnOne("Skill", Icon.called("book").build());
        Set<String> skills = petOptional
                .map(MyPet::getSkills)
                .map(Skills::getNames)
                .orElseGet(HashSet::new);
        for (String skill : skills) {
            skillTable.addRow(skill);
        }

        Icon pawIcon = Icon.called("paw").of(Color.GREEN).build();
        return newExtensionDataBuilder()
                .addValue(Boolean.class, valueBuilder("Has pet")
                        .priority(100)
                        .icon(pawIcon)
                        .buildBoolean(hasPet))
                .addValue(String.class, valueBuilder("Pet name")
                        .priority(90)
                        .icon(pawIcon)
                        .buildString(petName))
                .addValue(String.class, valueBuilder("Pet type")
                        .priority(80)
                        .icon(pawIcon)
                        .buildString(petType))
                .addTable("petSkills", skillTable.build(), Color.GREEN, "Pet skills");
    }

    private MyPetPlayer getMyPetPlayer(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) throw new NotReadyException(); // Player not present

        PlayerManager manager = myPet.getPlayerManager();
        if (manager == null) throw new NotReadyException(); // Plugin not enabled

        MyPetPlayer myPetPlayer = manager.getMyPetPlayer(player);
        if (myPetPlayer == null) throw new NotReadyException(); // No MyPet Data
        return myPetPlayer;
    }
}