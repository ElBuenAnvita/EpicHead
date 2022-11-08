# Comandos
Donde `<>` es un argumento obligatorio y `[]` es un argumento opcional

## Back
| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/back` | Regresa al último lecho de muerte conocido | `epiclol.back` | `/back` |

## Economía
| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/balance [player]`<br>`/bal [player]`<br>`/money [player]`<br>`/oro [player]` | Conoce el balance propio o el de otra persona | `epiclol.balance.own` <br> `epiclol.balance.other` | `/bal Anvita` |
| `/pay <player> <amount>` | Transfiere dinero de tu cuenta a un jugador<br><li>`player`: El jugador al cual le transferirás</li><li>`amount`: La cantidad que transferirás</li> | `epiclol.pay` | `/pay Anvita 3.2` |

## EpicHead
El comando base puede ser tanto `/epichead` como `/eh`

| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/eh help` | Lista los comandos que pertenecen a esta categoría | `epiclol.admin` | `/eh help` |
| `/eh reload <file>` | Recarga cualquier archivo del plugin excepto el `config.yml` y el `acf-messages.yml`<br><li>`file`: Nombre del archivo a recargar</li> | `epiclol.admin.reload` | `/eh reload warps` |
| `/eh setspawn` | Reubica o ubica por primera vez la ubicación de reaparición | `epiclol.admin`<br>`epiclol.admin.setspawn` | `/eh setspawn` |
| `/eh warp add <name> [displayname]`<br>`/eh warp add <name> [displayname]` | Crea una _warp_ y establece su punto de aparición<br><li>`name`: Nombre del _warp_</li><li>`displayname`: Nombre con formato[^1]</li> | `epiclol.admin`<br>`epiclol.admin.warp.add` | `/eh warp add selva`<br><br>`/eh warp add selva <green>Selva</green>` |
| `/eh warp relocate <name>`<br>`/eh warp rel <name>`<br>`/eh warp set <name>` | Establece el punto de aparición de una _warp_<br><li>`name`: Nombre del _warp_</li> | `epiclol.admin`<br>`epiclol.admin.warp.set` | `/eh warp rel selva` |
| `/eh warp delete <name>`<br>`/eh warp del <name>` | Elimina una _warp_<br><li>`name`: Nombre del _warp_</li> | `epiclol.admin`<br>`epiclol.admin.warp.del` | `/eh warp del selva` |
| `/eh warp eco add <player> <amount>`<br>`/eh warp eco give <player> <amount>`<br>`/eh warp eco deposit <player> <amount>` | Deposita dinero a la cuenta de un jugador<br><li>`player`: Jugador al que se le depositará</li><li>`amount`: La cantidad de dinero a depositar</li> | `epiclol.admin`<br>`epiclol.admin.eco.add` | `/eh eco add Anvita 20` |
| `/eh warp eco remove <player> <amount>`<br>`/eh warp eco withdraw <player> <amount>` | Retira dinero a la cuenta de un jugador<br><li>`player`: Jugador al que se le retirará</li><li>`amount`: La cantidad de dinero a retirar</li> | `epiclol.admin`<br>`epiclol.admin.eco.remove` | `/eh eco remove Anvita 100` |

## Homes/Hogares
| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/home`<br>`/home list`<br>`/homes` | Lista las casas que se poseen | N/A | `/home` |
| `/home <name>`<br>`/home teleport <name>`<br>`/home tp <name>` | Inicia una petición de teletransportación hacia el hogar indicado<br><li>`name`: Nombre del hogar a donde transportarse</li> | N/A | `/home peces` |
| N/A | Limita la cantidad de hogares que un usuario puede tener<br><li>`amount`: Cantidad de hogares máximas permitidas</li> | `epiclol.homes.<amount>` | `epiclol.homes.10` | 
| `/sethome <name>`<br>`/home set <name>`<br>`/home add <name>` | Crea un hogar y establece su punto de aparición<br><li>`name`: Nombre del nuevo hogar</li> | `epiclol.home.set` | `/sethome peces` |
| `/delhome <name>`<br>`/home del <name>`<br>`/home delete <name>`<br>`/home remove <name>` | Elimina un hogar<br><li>`name`: Nombre del hogar a eliminar</li> | `epiclol.home.del` | `/delhome peces` |

## Kits
Los comandos que inician con `/kit admin (...)` pueden iniciar de igual modo con `/adminkit (...)`

| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/kit` | Abre una interfaz (GUI) al jugador para reclamar un kit | `epiclol.kit` | `/kit` |
| `/kit claim <kit>` | Reclama un kit sin abrir la interfaz <br /><li>`kit`: Kit a reclamar</li> | `epiclol.kit` | `/kit claim default` |
| `/kit admin reload` | Recarga los kits | `epiclol.admin.kit`<br>`epiclol.admin.kit.reload` | `/kit admin reload` |
| `/kit admin give <kit> <player>` | Regala un kit a un jugador<br>*Importante:* El kit internamente será contado como reclamado<br>*Importante:* El jugador NO será cobrado por recibirlo<br><li>`kit`: Nombre del kit a dar</li><li>`player`: Jugador que recibirá el kit</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.give` | `/kit admin give default Anvita` |
| `/kit admin create <kit>` | Crea un kit<br><li>`kit`: Nombre del nuevo kit</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.create` | `/kit admin create default` |
| `/kit admin set displayname <kit> <displayname>` | Establece un nombre para mostrar<br><li>`kit`: Nombre del kit a modificar</li><li>`displayname`: Nombre con formato[^1]</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.set.displayname` | `/kit admin set displayname default <bold>Kit por defecto</bold>` |
| `/kit admin set delay <kit> <seconds>` | Establece un tiempo de espera para reclamar este kit<br><li>`kit`: Nombre del kit a modificar</li><li>`seconds`: Tiempo en segundos a esperar (admite decimales para _ms_). Utilice `-1` si quiere que no haya espera alguna.</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.set.duration` | `/kit admin set delay default -1` |
| `/kit admin set guiitem <kit>` | Establece el item mostrado en la interfaz para ese kit. Se usará el item que sostiene en su mano principal<br><li>`kit`: Nombre del kit a modificar</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.set.guiitem` | `/kit admin set guiitem default` |
| `/kit admin set price <kit> <amount>` | Establece el precio que se cobrará al usuario al reclamar el kit<br><li>`kit`: Nombre del kit a modificar</li><li>`amount`: Cantidad de dinero a cobrar. No puede ser menor que `0`</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.set.price` | `/kit admin set price 10` |
| `/kit admin edit additem <kit>` | Añade un item a la lista de items para entregar cuando el usuario reclame el kit. Se usará el item que sostiene en su mano principal<br><li>`kit`: Nombre del kit a modificar</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.edit.items` | `/kit admin edit additem` |
| `/kit admin edit listitem <kit>`<br>`/kit admin edit listitems <kit>` | Lista los items que contiene un kit<br><li>`kit`: Nombre del kit a usar</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.edit.items` | `/kit admin edit listitems` |
| `/kit admin edit removeitem <kit> <index>` | Elimina un item de la lista de items que tiene un kit<br><li>`kit`: Nombre del kit a usar</li><li>`index`: Índice del item en esa lista. Es el numero que aparece entre `[]` en el comando anterior.</li> | `epiclol.admin.kit`<br>`epiclol.admin.kit.edit.items` | `/kit admin removeitem 2` |

## Spawn
| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/spawn` | Inicia un proceso de teletransportación hacia el punto de reaparición principal | N/A | `/spawn` |

## Teletransporte
| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/tpa <player>`<br>`/tpa send <player>` | Envía una petición de teletransporte<br><li>`player`: Jugador al que se le enviará la petición para ir hacia este</li> | N/A | `/tpa Anvita` |
| `/tpah <player>`<br>`/tpahere <player>` | Envía una petición de teletransporte invertido<br><li>`player`: Jugador al que se le enviará la petición para que se dirija hacia nosotros.</li> | N/A | `/tpahere Anvita` |
| `/tpaccept`<br>`/tpaaccept`<br>`/tpa accept` | Acepta la última petición de teletransporte que se recibió | N/A | `/tpaccept` |
| `/tpadeny`<br>`/tpdeny`<br>`/tpa deny`<br>`/tpa decline` | Rechaza la última petición de teletransporte que se recibió | N/A | `/tpadeny` |

## Warps
| Comandos | Descripción | Permiso | Ejemplo |  
| --- | --- | --- | --- |
| `/warp <name>` | Inicia una petición de teletransportación hacia una _warp_<br><li>`name`: Nombre de la warp</li> | N/A | `/warp selva` |

[^1]: Admite formato [MiniMessage](https://webui.adventure.kyori.net/). [Más información](https://docs.adventure.kyori.net/minimessage/format.html).
