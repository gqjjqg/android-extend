/*
 * =============================================================================
 *
 *       Filename:  rbtree.h
 *
 *    Description:  rbtree(Red-Black tree) implementation adapted from linux
 *                  kernel thus can be used in userspace c program.
 *
 *        Created:  09/02/2012 11:36:11 PM
 *
 *         Author:  Fu Haiping (forhappy), haipingf@gmail.com
 *        Company:  ICT ( Institute Of Computing Technology, CAS )
 *
 * =============================================================================
 */

/*
  Red Black Trees
  (C) 1999  Andrea Arcangeli <andrea@suse.de>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  linux/include/linux/rbtree.h

  To use rbtrees you'll have to implement your own insert and search cores.
  This will avoid us to use callbacks and to drop drammatically performances.
  I know it's not the cleaner way,  but in C (not in C++) to get
  performances and genericity...

  Some example of insert and search follows here. The search is a plain
  normal search over an ordered tree. The insert instead must be implemented
  in two steps: First, the code must insert the element in order as a red leaf
  in the tree, and then the support library function rb_insert_color() must
  be called. Such function will do the not trivial work to rebalance the
  rbtree, if necessary.

-----------------------------------------------------------------------
static inline struct page * rb_search_page_cache(struct inode * inode,
						 unsigned long offset)
{
	struct rb_node * n = inode->i_rb_page_cache.rb_node;
	struct page * page;

	while (n)
	{
		page = rb_entry(n, struct page, rb_page_cache);

		if (offset < page->offset)
			n = n->rb_left;
		else if (offset > page->offset)
			n = n->rb_right;
		else
			return page;
	}
	return NULL;
}

static inline struct page * __rb_insert_page_cache(struct inode * inode,
						   unsigned long offset,
						   struct rb_node * node)
{
	struct rb_node ** p = &inode->i_rb_page_cache.rb_node;
	struct rb_node * parent = NULL;
	struct page * page;

	while (*p)
	{
		parent = *p;
		page = rb_entry(parent, struct page, rb_page_cache);

		if (offset < page->offset)
			p = &(*p)->rb_left;
		else if (offset > page->offset)
			p = &(*p)->rb_right;
		else
			return page;
	}

	rb_link_node(node, parent, p);

	return NULL;
}

static inline struct page * rb_insert_page_cache(struct inode * inode,
						 unsigned long offset,
						 struct rb_node * node)
{
	struct page * ret;
	if ((ret = __rb_insert_page_cache(inode, offset, node)))
		goto out;
	rb_insert_color(node, &inode->i_rb_page_cache);
 out:
	return ret;
}
-----------------------------------------------------------------------
*/

#ifndef	_RBTREE_H
#define	_RBTREE_H

typedef struct rb_node_t {
	unsigned long  rb_parent_color;
	struct rb_node_t *rb_right;
	struct rb_node_t *rb_left;
}RB_NODE, *LPRB_NODE;

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

#endif	/* _LINUX_RBTREE_H */

