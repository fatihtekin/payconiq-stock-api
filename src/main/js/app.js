'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');

class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {stocks: []};
	}

	componentDidMount() {
		client({method: 'GET', path: '/api/stocks'}).done(response => {
			this.setState({stocks: response.entity});
		});
	}

	render() {
		return (
			<StockList stocks={this.state.stocks}/>
		)
	}
}

class StockList extends React.Component{
	render() {
		var stocks = this.props.stocks.map(stock =>
            <Stock key={stock.id} stock={stock}/>);
		return (
			<table>
				<tbody>
					<tr>
						<th>Id</th>
						<th>Name</th>
						<th>CurrentPrice</th>
                        <th>LastUpdate</th>
					</tr>
					{stocks}
				</tbody>
			</table>
		)
	}
}

class Stock extends React.Component{
	render() {
		return (
			<tr>
				<td>{this.props.stock.id}</td>
				<td>{this.props.stock.name}</td>
				<td>{this.props.stock.currentPrice}</td>
                <td>{new Date(this.props.stock.lastUpdate).toLocaleString()}</td>
			</tr>
		)
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
)

