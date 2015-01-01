#ifndef	_DOUBLE_LINKED_LIST_H
#define	_DOUBLE_LINKED_LIST_H

typedef struct double_link_node_t {
	struct double_link_node_t *dl_pre;
	struct double_link_node_t *dl_next;
}DL_NODE, *LPDL_NODE;

typedef struct double_link_root_t {
	LPDL_NODE dl_head;
	LPDL_NODE dl_last;
}DL_ROOT, *LPDL_ROOT;

#ifdef __cplusplus
extern "C"{
#endif

void dl_init_node(LPDL_NODE node);
void dl_insert_node(LPDL_NODE node, LPDL_NODE insert, LPDL_ROOT root);
void dl_remove_node(LPDL_NODE node, LPDL_ROOT root);
void dl_replace_node(LPDL_NODE node, LPDL_NODE replace, LPDL_ROOT root);

/* Find logical next and previous nodes */
LPDL_NODE dl_next(LPDL_NODE node);
LPDL_NODE dl_prev(LPDL_NODE node);
LPDL_NODE dl_first(const LPDL_ROOT root);
LPDL_NODE dl_last(const LPDL_ROOT root);

#ifdef __cplusplus
}
#endif

#endif
