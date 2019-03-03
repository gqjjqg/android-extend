#ifndef	_RBTREE_H
#define	_RBTREE_H

#ifndef INT64
#define INT unsigned long
#else
#define INT unsigned long long
#endif

typedef struct rb_node_t {
	INT rb_parent_color;
	struct rb_node_t *rb_right;
	struct rb_node_t *rb_left;
}__attribute__((aligned(sizeof(long)))) RB_NODE, *LPRB_NODE;

typedef struct rb_root_t {
	LPRB_NODE rb_node;
}RB_ROOT, *LPRB_ROOT;

#ifdef __cplusplus
extern "C"{
#endif

void rb_init_node(LPRB_NODE rb);
void rb_link_node(LPRB_NODE node, LPRB_NODE  parent, LPRB_NODE * rb_link);
void rb_insert_color(LPRB_NODE , LPRB_ROOT );
void rb_erase(LPRB_NODE , LPRB_ROOT );

/* Find logical next and previous nodes in a tree */
LPRB_NODE rb_next(LPRB_NODE );
LPRB_NODE rb_prev(LPRB_NODE );
LPRB_NODE rb_first(const LPRB_ROOT );
LPRB_NODE rb_last(const LPRB_ROOT );

/* Fast replacement of a single node without remove/rebalance/add/rebalance */
void rb_replace_node(LPRB_NODE victim, LPRB_NODE new, LPRB_ROOT root);

#ifdef __cplusplus
}
#endif

#endif


