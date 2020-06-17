import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import {Button, List, Spin, Modal} from "antd";
import { ordersService } from './OrdersService';
import OrderModal from "./components/OrderModal";
import {withRouter} from "react-router-dom";
import {commonService} from "../Common/CommonService";


class OrdersView extends Component {

    constructor(props) {
        super(props);

        this.state = {
            orders: [],
            visible: true,
            loading: true
        };
    }

    componentDidMount() {
        commonService.checkIfAuthenticated(this.props).then(result => {
            if(result) this.loadProducts();
        });
    }

    uniq(a) {
        return Array.from(new Set(a));
    }

    loadProducts = () => {
        ordersService.getOrdersForUser().then( async ids => {
            let orderIds = this.uniq(ids.orders);
            let orders = [];

            let promises = orderIds.map( async orderId => {
                let order = await ordersService.getOrderById(orderId);
                orders.push(order);

                return order;
            });

            Promise.all(promises).then(() => {
                this.setState({orders: orders, loading: false});
            });

        });
    }

    showDetails = (orderId) => {
        let filteredId= this.state.orders.findIndex(o => o.order.id === orderId);
        let products = this.state.orders[filteredId].products;
        this.refs.orderModal.show(products);
    }

    render() {
        return(
            <div className="bucketView">
                <Spin spinning={this.state.loading} className='spin' size="large">
                    <List
                        itemLayout="vertical"
                        size="large"
                        locale={{emptyText: 'No products in bucket'}}
                        style={{marginLeft: '25%', marginRight: '25%'}}
                        pagination={{
                            size: "small",
                            pageSize: 6,
                        }}
                        dataSource={this.state.orders}
                        renderItem={item => (
                            <List.Item
                                key={item.order.id}
                                style={{textAlign: "left"}}
                                extra={
                                    <Button style={{marginBottom: '10px'}}
                                            onClick={ e => this.showDetails(item.order.id) }>
                                            Details</Button>
                                }
                            >
                                <List.Item.Meta
                                    style={{textAlign: "left"}}
                                    title={<strong>Order {item.order.id}</strong>}
                                    description={<strong>Status: {item.order.status}</strong>}
                                />
                                Products:
                                {item.products.map( p => ` ${p} ` )}
                            </List.Item>
                        )}
                    />
                    <OrderModal ref='orderModal' />
                </Spin>
            </div>
        )
    }
}

export default withRouter(OrdersView);