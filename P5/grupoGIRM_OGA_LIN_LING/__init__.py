"""El paquete de vuestro grupo. Cambiad YY por vuestra letra en el nombre de la carpeta."""
from .mcts import MCTS, MCTSRival, MCTSCapturas, MCTSRivalCapturas
from .minimax import Minimax


def jugador():
    """La versión que entregáis como la vuestra: sin argumentos, y con el nombre de vuestro grupo.
    Puede ser un MCTS, un Minimax o lo vuestro: lo que mejor os haya ido en comparar.py."""
    return MCTSRival(nombre="grupoGIRM_OGA_LIN_LING")
