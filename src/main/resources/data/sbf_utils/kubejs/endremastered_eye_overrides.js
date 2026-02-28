ServerEvents.recipes(event => {
  const eyes = {
    black_eye: 'endrem:black_eye',
    lost_eye: 'endrem:lost_eye',
    old_eye: 'endrem:old_eye',
    cold_eye: 'endrem:cold_eye',
    rogue_eye: 'endrem:rogue_eye',
    cryptic_eye: 'endrem:cryptic_eye',
    magical_eye: 'endrem:magical_eye',
    corrupted_eye: 'endrem:corrupted_eye',
    cursed_eye: 'endrem:cursed_eye'
  }

  const i = {
    withered_spider_eye: 'cataclysm:withered_spider_eye',
    dragonsteel_lightning_ingot: 'iceandfire:dragonsteel_lightning_ingot',
    cockatrice_eye: 'iceandfire:cockatrice_eye',
    rift: 'alexscaves:rift',
    starstruck_scrap: 'alexscaves:starstruck_scrap',

    revive_crystal: 'cataclysm:revive_crystal',
    ominous_catalyst: 'dungeonsnowloading:ominous_catalyst',
    celestial_netherite_ingot: 'celestisynth:celestial_netherite_ingot',
    tectonic_shard: 'dungeons_and_combat:tectonic_shard',
    blood_orb: 'irons_spellbooks:blood_orb',
    crimson_pearl: 'aquamirae:crimson_pearl',

    sol_visage: 'call_of_yucatan:sol_visage',
    ancient_pearl: 'aquamirae:ancient_pearl',
    relic_gold: 'call_of_yucatan:relic_gold',
    death_worm_chitin: 'iceandfire:deathworm_chitin',
    essence_of_sunleia: 'celestisynth:essence_of_sunleia',
    sandstorm_in_a_bottle: 'call_of_yucatan:sandstorm_in_a_bottle',

    echo_of_the_ship_graveyard: 'aquamirae:echo_of_the_ship_graveyard',
    rune_of_the_storm: 'dungeons_and_combat:rune_of_the_storm',
    ice_crystal: 'iceandfire:ice_crystal',
    icy_pearl: 'aquamirae:icy_pearl',
    fin: 'aquamirae:fin',

    catalyzed_redstone: 'dungeons_and_combat:catalyzed_redstone',
    combustion_cell: 'dungeons_and_combat:combustion_cell',
    fragment_of_rebirth: 'cataclysm:fragment_of_rebirth',
    fragment_of_death: 'cataclysm:fragment_of_death',
    last_glow: 'dungeons_and_combat:last_glow',
    scarlet_neodymium_ingot: 'alexscaves:scarlet_neodymium_ingot',
    azure_neodymium_ingot: 'alexscaves:azure_neodymium_ingot',
    fissile_core: 'alexscaves:fissile_core',

    nugget_of_experience: 'dungeons_and_combat:nugget_of_experience',
    spawner_blade: 'dungeons_and_combat:spawner_blade',
    chaotic_hexedron: 'dungeons_and_combat:chaotic_hexedron',

    cursium_ingot: 'cataclysm:cursium_ingot',
    abyssal_spellweave_ingot: 'irons_spellbooks:abyssal_spellweave_ingot',
    ignitium_ingot: 'cataclysm:ignitium_ingot',
    resonant_scrap: 'alexscaves:resonant_scrap',
    lacrima: 'aquamirae:lacrima',
    divine_pearl: 'aquamirae:divine_pearl',

    verdant_spellweave_ingot: 'irons_spellbooks:verdant_spellweave_ingot',
    hemolymph_sac: 'alexscaves:hemolymph_sac',
    divine_soulshard: 'irons_spellbooks:divine_soulshard',
    scroll_of_the_dead_sea: 'aquamirae:scroll_of_the_dead_sea',

    sandy_diamond_block: 'call_of_yucatan:sandy_diamond_block',
    scarlet_crown: 'scarlet_king:scarlet_crown',
    lava_power_cell: 'dungeons_and_combat:lava_power_cell',
    withered_nether_star: 'cataclysm:withered_nether_star',
    vile_herb: 'call_of_yucatan:vile_herb'
  }

  const repeat = (itemId, count) => Array(count).fill(itemId)

  // remove original End Remastered eye recipes
  Object.values(eyes).forEach(eye => event.remove({ output: eye }))

  event.shapeless(eyes.black_eye, [
    i.withered_spider_eye,
    ...repeat(i.dragonsteel_lightning_ingot, 2),
    i.cockatrice_eye,
    ...repeat(i.rift, 2),
    ...repeat(i.starstruck_scrap, 2)
  ])

  event.shapeless(eyes.lost_eye, [
    i.revive_crystal,
    i.ominous_catalyst,
    i.celestial_netherite_ingot,
    ...repeat(i.tectonic_shard, 4),
    i.blood_orb,
    i.crimson_pearl
  ])

  event.shapeless(eyes.old_eye, [
    i.sol_visage,
    i.ancient_pearl,
    i.relic_gold,
    ...repeat(i.death_worm_chitin, 4),
    i.essence_of_sunleia,
    i.sandstorm_in_a_bottle
  ])

  event.shapeless(eyes.cold_eye, [
    i.echo_of_the_ship_graveyard,
    i.rune_of_the_storm,
    i.ice_crystal,
    i.icy_pearl,
    ...repeat(i.fin, 4)
  ])

  event.shapeless(eyes.rogue_eye, [
    Ingredient.of([i.catalyzed_redstone, i.combustion_cell]),
    Ingredient.of([i.fragment_of_rebirth, i.fragment_of_death]),
    i.last_glow,
    i.scarlet_neodymium_ingot,
    i.azure_neodymium_ingot,
    i.fissile_core
  ])

  event.shapeless(eyes.cryptic_eye, [
    ...repeat(i.nugget_of_experience, 4),
    ...repeat(i.spawner_blade, 4),
    i.chaotic_hexedron
  ])

  event.shapeless(eyes.magical_eye, [
    i.cursium_ingot,
    i.abyssal_spellweave_ingot,
    i.ignitium_ingot,
    ...repeat(i.resonant_scrap, 4),
    i.lacrima,
    i.divine_pearl
  ])

  event.shapeless(eyes.corrupted_eye, [
    ...repeat(i.verdant_spellweave_ingot, 4),
    i.hemolymph_sac,
    i.divine_soulshard,
    i.scroll_of_the_dead_sea
  ])

  event.shapeless(eyes.cursed_eye, [
    ...repeat(i.sandy_diamond_block, 4),
    i.scarlet_crown,
    ...repeat(i.lava_power_cell, 2),
    i.withered_nether_star,
    i.vile_herb
  ])
})