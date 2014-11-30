#ifndef	_DOUBLE_LINKED_LIST_H
#define	_DOUBLE_LINKED_LIST_H

typedef struct double_link_node_t {
	struct double_link_node_t *dll_pre;
	struct double_link_node_t *dll_next;
}DLL_NODE, *LPDLL_NODE;

typedef struct double_link_root_t {
	LPDLL_NODE dll_head;
	LPDLL_NODE dll_last;
}DLL_ROOT, *LPDLL_ROOT;

#ifdef __cplusplus
extern "C"{
#endif

void dll_init_node(LPDLL_NODE node);
void dll_insert_node(LPDLL_NODE node, LPDLL_NODE insert, LPDLL_ROOT root);
void dll_remove_node(LPDLL_NODE node, LPDLL_ROOT root);
void dll_replace_node(LPDLL_NODE node, LPDLL_NODE replace, LPDLL_ROOT root);

/* Find logical next and previous nodes */
LPDLL_NODE dll_next(LPDLL_NODE node);
LPDLL_NODE dll_prev(LPDLL_NODE node);
LPDLL_NODE dll_first(const LPDLL_ROOT root);
LPDLL_NODE dll_last(const LPDLL_ROOT root);

#ifdef __cplusplus
}
#endif

#endif
