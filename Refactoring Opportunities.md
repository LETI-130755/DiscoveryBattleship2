| local | nome do cheiro no código | nome da refabricação | número de alun@ |
| :--- | :--- | :--- | :--- |
| `Game::fireSingleShot` | Duplicate expression (`!ship.stillFloating()`) | Introduce Variable | 129808 |
| `Game::printBoard` | Long method / Print legend responsibility | Extract Method | 129808 |
| `Game::printLegend` | Magic literal / Hardcoded legend header string | Introduce Constant | 129808 |
| `Game::fireSingleShot` | Unclear variable name (`b`) | Rename | 129808 |
| `Game::fireSingleShot` | Temporary local variable (`ship`) | Inline Variable | 129808 |
| `Game::printLegend` | Fixed legend header dependency / Hardcoded argument | Introduce Parameter | 129808 |
| `Fleet::createRandom` | Long Method | Extract Method | 98458 |
| `Ship::getAdjacentPositions` | Expressions & Conditionals | Introduce Variable | 98458 |
| `Fleet::printShips` | Feature Envy (MethodMayBeStatic) | Make Static | 98458 |
| `GameController::receiveShots` | Magic Number (`10` na validação da coluna) | Introduce Constant | 98458 |
| `Fleet::addShip` (parâmetro `s`) | Mysterious Name (nome de uma só letra) | Rename | 98458 |